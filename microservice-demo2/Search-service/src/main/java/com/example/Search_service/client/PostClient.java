package com.example.Search_service.client;

import com.example.Search_service.document.PostDocument;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "post-service", path = "/api/posts")
public interface PostClient {
    @GetMapping
    List<PostDocument> getAllPosts();
}
