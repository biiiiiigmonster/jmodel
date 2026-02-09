package io.github.biiiiiigmonster.entity;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.relation.annotation.BelongsToMany;
import io.github.biiiiiigmonster.relation.annotation.HasMany;
import io.github.biiiiiigmonster.relation.annotation.HasManyThrough;
import io.github.biiiiiigmonster.relation.annotation.HasOne;
import io.github.biiiiiigmonster.relation.annotation.HasOneThrough;
import io.github.biiiiiigmonster.relation.annotation.MorphOne;
import io.github.biiiiiigmonster.relation.annotation.config.MorphAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@MorphAlias
public class User extends Model<User> {
    private Long id;
    private String name;
    private String email;

    @HasOne
    private Phone phone;

    @HasOne
    private Profile profile;

    @HasOneThrough(through = Profile.class)
    private Address profileAddress;

    @HasMany(foreignKey = "userId")
    private List<Post> posts;

    @HasMany(foreignKey = "userId", chaperone = true)
    private List<Post> postChaperones;

    @HasManyThrough(through = Post.class)
    private List<Likes> commentLikes;

    @BelongsToMany(using = UserRole.class)
    private List<Role> roles;

    @BelongsToMany(using = UserRole.class, withPivot = true)
    private List<Role> roleWithPivots;

    @MorphOne
    private Image image;
}
