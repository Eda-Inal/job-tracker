package com.jobtracker.tag.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponse {

    private Long id;
    private Long userId;
    private String name;
    private String color;
}
