package io.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.biiiiiigmonster.relation.Pivot;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName
@EqualsAndHashCode(callSuper = false)
public class UserRole extends Pivot<UserRole> {
    @TableId
    private Long id;
    private Long userId;
    private Long roleId;
}