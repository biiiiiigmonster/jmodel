package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class User extends Model<User> {

    private Long id;
    private String name;
}
