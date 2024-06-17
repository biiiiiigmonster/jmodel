package com.biiiiiigmonster.octopus.models;

import com.biiiiiigmonster.octopus.eloquent.EloquentBuilder;
import com.biiiiiigmonster.octopus.eloquent.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/11/21 19:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class User extends Model<User> {
    private Long id;
    private String name;
    private String email;

    public static EloquentBuilder<User> query() {
        return new User().newQuery();
    }
}
