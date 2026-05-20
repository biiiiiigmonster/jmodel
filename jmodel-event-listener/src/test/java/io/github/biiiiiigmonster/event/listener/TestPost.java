package io.github.biiiiiigmonster.event.listener;

import io.github.biiiiiigmonster.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPost extends Model<TestPost> {

    private Long id;
    private String title;
}
