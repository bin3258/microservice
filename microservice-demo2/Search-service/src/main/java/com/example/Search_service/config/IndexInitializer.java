package com.example.Search_service.config;

import com.example.Search_service.client.PostClient;
import com.example.Search_service.client.ProductClient;
import com.example.Search_service.repository.PostSearchRepository;
import com.example.Search_service.repository.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class IndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IndexInitializer.class);

    private final ProductClient productClient;
    private final PostClient postClient;
    private final ProductSearchRepository productSearchRepository;
    private final PostSearchRepository postSearchRepository;

    public IndexInitializer(ProductClient productClient, PostClient postClient,
                            ProductSearchRepository productSearchRepository,
                            PostSearchRepository postSearchRepository) {
        this.productClient = productClient;
        this.postClient = postClient;
        this.productSearchRepository = productSearchRepository;
        this.postSearchRepository = postSearchRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Indexing existing products from product-service...");
            var products = productClient.getAllProducts();
            productSearchRepository.saveAll(products);
            log.info("Indexed {} products into Elasticsearch", products.size());
        } catch (Exception e) {
            log.warn("Failed to index products: {}", e.getMessage());
        }

        try {
            log.info("Indexing existing posts from post-service...");
            var posts = postClient.getAllPosts();
            postSearchRepository.saveAll(posts);
            log.info("Indexed {} posts into Elasticsearch", posts.size());
        } catch (Exception e) {
            log.warn("Failed to index posts: {}", e.getMessage());
        }
    }
}
