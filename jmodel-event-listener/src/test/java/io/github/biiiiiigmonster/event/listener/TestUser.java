package io.github.biiiiiigmonster.event.listener;

import io.github.biiiiiigmonster.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestUser extends Model<TestUser> {

    private Long id;
    private String name;
}
