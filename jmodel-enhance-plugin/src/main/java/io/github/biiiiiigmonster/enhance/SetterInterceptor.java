package io.github.biiiiiigmonster.enhance;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.pool.TypePool;

/**
 * Setter 方法拦截器（字节码增强）。
 * <p>
 * 在 setter 方法体开头注入 {@code this.$jmodel$trackChange(fieldName, oldValue, newValue)} 调用，
 * 实现字段变更的实时追踪。
 * <p>
 * 增强前：
 * <pre>
 * public void setName(String name) {
 *     this.name = name;
 * }
 * </pre>
 * 增强后：
 * <pre>
 * public void setName(String name) {
 *     this.$jmodel$trackChange("name", this.name, name);
 *     this.name = name;
 * }
 * </pre>
 * <p>
 * 支持所有 Java 基本类型和引用类型，基本类型自动装箱为 Object。
 *
 * @author luyunfeng
 */
public class SetterInterceptor implements AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper {

    /**
     * Model 基类的内部名称（用于 INVOKEVIRTUAL 指令）
     */
    private static final String MODEL_INTERNAL_NAME = "io/github/biiiiiigmonster/Model";

    /**
     * $jmodel$trackChange 方法名
     */
    private static final String TRACK_CHANGE_NAME = "$jmodel$trackChange";

    /**
     * $jmodel$trackChange 方法描述符：(String, Object, Object) -> void
     */
    private static final String TRACK_CHANGE_DESC = "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V";

    private final String fieldName;
    private final String fieldDescriptor;
    private final String fieldOwnerInternalName;

    /**
     * @param fieldName              字段名（如 "name"）
     * @param fieldDescriptor        字段的 JVM 类型描述符（如 "Ljava/lang/String;"、"I"）
     * @param fieldOwnerInternalName 字段所在类的内部名称（如 "com/example/User"）
     */
    public SetterInterceptor(String fieldName, String fieldDescriptor, String fieldOwnerInternalName) {
        this.fieldName = fieldName;
        this.fieldDescriptor = fieldDescriptor;
        this.fieldOwnerInternalName = fieldOwnerInternalName;
    }

    @Override
    public MethodVisitor wrap(TypeDescription instrumentedType,
                              MethodDescription instrumentedMethod,
                              MethodVisitor methodVisitor,
                              Implementation.Context implementationContext,
                              TypePool typePool,
                              int writerFlags,
                              int readerFlags) {
        return new TrackingMethodVisitor(methodVisitor);
    }

    /**
     * 自定义 MethodVisitor，在方法体开头注入追踪代码
     */
    private class TrackingMethodVisitor extends MethodVisitor {

        TrackingMethodVisitor(MethodVisitor delegate) {
            super(Opcodes.ASM9, delegate);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            injectTrackingCall();
        }

        /**
         * 注入 {@code this.$jmodel$trackChange("fieldName", this.field, param)} 字节码
         * <p>
         * 生成的字节码序列（以 String 字段为例）：
         * <pre>
         * ALOAD 0                                          // push this
         * LDC "name"                                       // push field name
         * ALOAD 0                                          // push this (for GETFIELD)
         * GETFIELD com/example/User.name:Ljava/lang/String; // get current value
         * ALOAD 1                                          // push setter parameter
         * INVOKEVIRTUAL Model.$jmodel$trackChange(String, Object, Object)V
         * </pre>
         * 对于基本类型字段（如 int），会在 GETFIELD 和参数加载后插入自动装箱指令。
         */
        private void injectTrackingCall() {
            // 1. Push 'this'（方法调用的接收者）
            super.visitVarInsn(Opcodes.ALOAD, 0);

            // 2. Push 字段名常量
            super.visitLdcInsn(fieldName);

            // 3. Push 当前字段值 (this.field)，基本类型自动装箱
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitFieldInsn(Opcodes.GETFIELD, fieldOwnerInternalName, fieldName, fieldDescriptor);
            emitBoxing(fieldDescriptor);

            // 4. Push setter 参数值，基本类型自动装箱
            Type fieldType = Type.getType(fieldDescriptor);
            int loadOpcode = fieldType.getOpcode(Opcodes.ILOAD);
            super.visitVarInsn(loadOpcode, 1);
            emitBoxing(fieldDescriptor);

            // 5. 调用 $jmodel$trackChange
            super.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                MODEL_INTERNAL_NAME,
                TRACK_CHANGE_NAME,
                TRACK_CHANGE_DESC,
                false
            );
        }

        /**
         * 为基本类型生成自动装箱指令。引用类型不做任何操作。
         * <p>
         * 例如：{@code int} → {@code Integer.valueOf(int)}
         */
        private void emitBoxing(String descriptor) {
            switch (descriptor.charAt(0)) {
                case 'Z':
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf",
                        "(Z)Ljava/lang/Boolean;", false);
                    break;
                case 'B':
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf",
                        "(B)Ljava/lang/Byte;", false);
                    break;
                case 'C':
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf",
                        "(C)Ljava/lang/Character;", false);
                    break;
                case 'S':
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf",
                        "(S)Ljava/lang/Short;", false);
                    break;
                case 'I':
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf",
                        "(I)Ljava/lang/Integer;", false);
                    break;
                case 'J':
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
                        "(J)Ljava/lang/Long;", false);
                    break;
                case 'F':
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf",
                        "(F)Ljava/lang/Float;", false);
                    break;
                case 'D':
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf",
                        "(D)Ljava/lang/Double;", false);
                    break;
                default:
                    // 引用类型（L...;）或数组类型（[...）无需装箱
                    break;
            }
        }
    }
}
