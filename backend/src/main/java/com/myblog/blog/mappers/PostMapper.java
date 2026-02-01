package com.myblog.blog.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.myblog.blog.domain.CreatePostRequest;
import com.myblog.blog.domain.UpdatePostRequest;
import com.myblog.blog.domain.dtos.CreatePostRequestDto;
import com.myblog.blog.domain.dtos.PostDto;
import com.myblog.blog.domain.dtos.UpdatePostRequestDto;
import com.myblog.blog.domain.entities.Post;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "status", source = "status")
    PostDto toDto(Post post);

    CreatePostRequest toCreatePostRequest(CreatePostRequestDto dto);

    UpdatePostRequest toUpdatePostRequest(UpdatePostRequestDto dto);

}
