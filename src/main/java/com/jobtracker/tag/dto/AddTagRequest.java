package com.jobtracker.tag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddTagRequest {

    @NotNull(message = "Tag ID is required")
    private Long tagId;
}
