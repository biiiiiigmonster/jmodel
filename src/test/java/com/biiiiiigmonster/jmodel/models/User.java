package com.biiiiiigmonster.jmodel.models;

import com.biiiiiigmonster.jmodel.eloquent.Model;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 19:40
 */
public class User extends Model<User> {
    private Long id;
    private String name;
    private Integer age;
    private String email;
}
