package com.blog;

import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.demo.read-only=true")
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:readonlyblogtest;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class ReadOnlyDemoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void publicPagesRemainAvailableAndAllWriteRoutesAreBlocked() throws Exception {
        Post post = postRepository.findAll().get(0);
        Comment comment = commentRepository.save(Comment.builder()
                .name("Read Only Test")
                .email("readonly@example.com")
                .comment("This comment must stay unchanged.")
                .post(post)
                .build());

        MvcResult homeResult = mockMvc.perform(get("/"))
                .andReturn();
        assertEquals(200, homeResult.getResponse().getStatus());
        MvcResult detailResult = mockMvc.perform(get("/posts/" + post.getId()))
                .andReturn();
        assertEquals(200, detailResult.getResponse().getStatus(), detailResult.getResponse().getContentAsString());

        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/posts")
                        .param("title", "Blocked Post")
                        .param("author", "Test Author")
                        .param("content", "This post must not be created."))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/posts/" + post.getId() + "/edit"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/posts/" + post.getId() + "/update")
                        .param("title", "Changed Title")
                        .param("author", "Changed Author")
                        .param("content", "Changed content"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/posts/" + post.getId() + "/delete"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/posts/" + post.getId() + "/comments")
                        .param("name", "Blocked Comment")
                        .param("email", "blocked@example.com")
                        .param("comment", "This comment must not be created."))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/posts/" + post.getId() + "/comments/" + comment.getId() + "/edit"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + comment.getId() + "/update")
                        .param("name", "Changed Name")
                        .param("email", "changed@example.com")
                        .param("comment", "Changed comment"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/posts/" + post.getId() + "/comments/" + comment.getId() + "/delete"))
                .andExpect(status().isForbidden());

        assertEquals(5L, postRepository.count());
        assertEquals(1L, commentRepository.count());
        assertEquals("This comment must stay unchanged.",
                commentRepository.findById(comment.getId()).orElseThrow().getComment());
    }
}
