package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Post extends Model<Post> {

    private Long id;
    private String title;
}
