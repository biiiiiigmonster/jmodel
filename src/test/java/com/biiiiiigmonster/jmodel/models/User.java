package com.biiiiiigmonster.octopus.models;

import com.biiiiiigmonster.octopus.eloquent.Model;

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
    private String email;
}
