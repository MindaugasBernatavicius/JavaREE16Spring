package cf.mindaugas.sdajavaree16springboot.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/inmemory/posts")
public class BlogPostInMemoryController {
    // fake database
    List<Blogpost> posts = new ArrayList<>(){{
       add(new Blogpost(1L, "First", ""));
       add(new Blogpost(2L, "Second", ""));
       add(new Blogpost(3L, "Third", ""));
    }};

    @GetMapping("")
    public List<Blogpost> getAllPosts(){
        return this.posts;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blogpost> getPostById(@PathVariable Long id){
        var bpOptional = this.posts.stream()
                .filter(bp -> bp.getId().equals(id)).findFirst();

        return bpOptional
                .map(blogpost -> new ResponseEntity<>(blogpost, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createPost(@RequestBody Blogpost blogpost){
        blogpost.setId((long) (posts.size() + 1));
        posts.add(blogpost);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id){
        var bpToDelete = this.posts
                .stream().filter(bp -> bp.getId().equals(id))
                .findFirst().get();
        posts.remove(bpToDelete);
    }

    @PutMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updatePost(@RequestBody Blogpost blogpost){
        // find the current value that we want to update
        var bpToUpdate = this.posts
                .stream().filter(bp -> bp.getId().equals(blogpost.getId()))
                .findFirst().get();

        // set the new fields on that value
        bpToUpdate.setText(blogpost.getText());
        bpToUpdate.setTitle(blogpost.getTitle());
    }
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Blogpost {
    private Long id;
    private String title;
    private String text;
}