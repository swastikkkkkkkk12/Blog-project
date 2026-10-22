package com.blog;

import com.blog.repository.PostRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.TagRepository;
import com.blog.entity.Comment;
import com.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:blogtest;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class BlogRequestValidationIntegrationTest {

    private static final String MISSING_ID = "9223372036854775807";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    @Qualifier("dataSeeder")
    private CommandLineRunner dataSeeder;

    @Test
    void seedsSamplePostsAndTagsOnApplicationStartup() {
        assertEquals(5L, postRepository.count());
        assertEquals(5L, tagRepository.count());
    }

    @Test
    void doesNotDuplicateSampleDataWhenRunAgain() throws Exception {
        dataSeeder.run();

        assertEquals(5L, postRepository.count());
        assertEquals(5L, tagRepository.count());
    }

    @Test
    void acceptsValidListingParameters() throws Exception {
        mockMvc.perform(get("/")
                        .param("start", "1")
                        .param("limit", "50")
                        .param("sortField", "title")
                        .param("order", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    void uncheckedPublishedFlagCreatesDraftHiddenFromPublicReadRoutes() throws Exception {
        String draftTitle = "Draft visibility integration test";
        String draftAuthor = "Draft Test Author";
        String draftTag = "DraftOnlyTag";

        mockMvc.perform(post("/posts")
                        .param("title", draftTitle)
                        .param("author", draftAuthor)
                        .param("content", "This post should remain a draft.")
                        .param("tagsCsv", draftTag))
                .andExpect(status().is3xxRedirection());

        Optional<Post> savedDraft = postRepository.findAll().stream()
                .filter(post -> draftTitle.equals(post.getTitle()))
                .findFirst();
        assertTrue(savedDraft.isPresent());
        assertFalse(savedDraft.orElseThrow().isPublished());

        MvcResult listResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();
        Page<?> listedPosts = (Page<?>) listResult.getModelAndView().getModel().get("posts");
        assertFalse(listedPosts.getContent().contains(savedDraft.orElseThrow()));

        MvcResult searchResult = mockMvc.perform(get("/").param("search", draftTitle))
                .andExpect(status().isOk())
                .andReturn();
        Page<?> searchedPosts = (Page<?>) searchResult.getModelAndView().getModel().get("posts");
        assertEquals(0L, searchedPosts.getTotalElements());

        MvcResult authorFilterResult = mockMvc.perform(get("/").param("author", draftAuthor))
                .andExpect(status().isOk())
                .andReturn();
        Page<?> authorFilteredPosts = (Page<?>) authorFilterResult.getModelAndView().getModel().get("posts");
        assertEquals(0L, authorFilteredPosts.getTotalElements());

        Long draftTagId = tagRepository.findByNameIgnoreCase(draftTag).orElseThrow().getId();
        MvcResult tagFilterResult = mockMvc.perform(get("/").param("tagId", draftTagId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        Page<?> tagFilteredPosts = (Page<?>) tagFilterResult.getModelAndView().getModel().get("posts");
        assertEquals(0L, tagFilteredPosts.getTotalElements());

        MvcResult authorAndTagFilterResult = mockMvc.perform(get("/")
                        .param("author", draftAuthor)
                        .param("tagId", draftTagId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        Page<?> authorAndTagFilteredPosts = (Page<?>) authorAndTagFilterResult.getModelAndView().getModel().get("posts");
        assertEquals(0L, authorAndTagFilteredPosts.getTotalElements());

        mockMvc.perform(get("/posts/" + savedDraft.orElseThrow().getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void unpublishingAPostRedirectsToPublicListing() throws Exception {
        Post post = postRepository.findAll().stream()
                .filter(Post::isPublished)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/posts/" + post.getId() + "/update")
                        .param("title", post.getTitle())
                        .param("author", post.getAuthor())
                        .param("content", post.getContent())
                        .param("excerpt", post.getExcerpt())
                        .param("tagsCsv", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/"));

        assertFalse(postRepository.findById(post.getId()).orElseThrow().isPublished());
        mockMvc.perform(get("/posts/" + post.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsStartBelowOne() throws Exception {
        mockMvc.perform(get("/").param("start", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsLimitOutsideAllowedRange() throws Exception {
        mockMvc.perform(get("/").param("limit", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/").param("limit", "51"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnsupportedSortFieldAndOrder() throws Exception {
        mockMvc.perform(get("/").param("sortField", "content"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/").param("order", "sideways"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForMissingPostAndPostDeletion() throws Exception {
        mockMvc.perform(get("/posts/" + MISSING_ID))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/posts/" + MISSING_ID + "/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundForMissingCommentAndCommentDeletion() throws Exception {
        mockMvc.perform(get("/posts/1/comments/" + MISSING_ID + "/edit"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/posts/1/comments/" + MISSING_ID + "/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsCommentRoutesWhenCommentBelongsToDifferentPost() throws Exception {
        Post commentPost = postRepository.findAll().get(0);
        Post routePost = postRepository.findAll().stream()
                .filter(post -> !post.getId().equals(commentPost.getId()))
                .findFirst()
                .orElseThrow();
        Comment savedComment = commentRepository.save(Comment.builder()
                .name("Comment Relationship Test")
                .email("relationship@example.com")
                .comment("Original comment text")
                .post(commentPost)
                .build());

        mockMvc.perform(get("/posts/" + routePost.getId() + "/comments/" + savedComment.getId() + "/edit"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/posts/" + routePost.getId() + "/comments/" + savedComment.getId() + "/update")
                        .param("name", "Changed Name")
                        .param("email", "changed@example.com")
                        .param("comment", "Changed comment text"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/posts/" + routePost.getId() + "/comments/" + savedComment.getId() + "/delete"))
                .andExpect(status().isNotFound());

        Comment unchangedComment = commentRepository.findById(savedComment.getId()).orElseThrow();
        assertEquals("Original comment text", unchangedComment.getComment());
        assertEquals(commentPost.getId(), unchangedComment.getPost().getId());
    }
}
