package cf.mindaugas.sdajavaree16springboot;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class SdaJavaRee16SpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(SdaJavaRee16SpringBootApplication.class, args);
    }

    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}

@Controller
class BlogPostMVCContoller {
    @Autowired
    private BlogpostRepository bpr;

    @GetMapping("/greeting")
    public String greeting(Model model, @RequestParam(required = false) String name){
        model.addAttribute("name_in_view", name);
        return "hello";
    }

    @GetMapping("/posts")
    public String displayPosts(Model model){
        var bps = bpr.findAll();
        model.addAttribute("posts", bps);
        return "posts";
    }

    @PostMapping(
            value = "/create-post",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createPost(Model model,
                             @RequestParam("title") String title,
                             @RequestParam("text") String text){
        var newBp = new Blogpost(title, text, new HashSet<>());
        bpr.save(newBp);
        // model.addAttribute("posts", bpr.findAll());
        // return "posts";
        return "redirect:/posts";
    }

    @GetMapping(path="/posts/delete/{id}")
    public String deleteById(Model model, @PathVariable Long id){
        bpr.deleteById(id);
        // http://localhost/posts/delete/35511566
        // model.addAttribute("posts", bpr.findAll());
        // return "posts";
        return "redirect:/posts";
    }
}

@RestController
@RequestMapping("/api/posts")
class BlogPostController {
    @Autowired
    private BlogpostRepository bpr;

    @GetMapping("")
    public Iterable<Blogpost> getAllPosts(
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String sortBy
    ){
        return sortBy != null && sortOrder != null
                ? this.bpr.findAll(Sort.by(getDirection(sortOrder), sortBy))
                : this.bpr.findAllPostsOrderByText();
    }

    // ... remember you can use methods that are not marked with
    // ... controller annotations inside controllers (these
    // ... would be called utility methods, and they might be private)
    private Sort.Direction getDirection(String rawDirection){
        return rawDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
    }

    @GetMapping("/bySubstring/{titleSubstring}")
    public Iterable<Blogpost> getAllPostsByTitleContains(@PathVariable String titleSubstring){
        return bpr.findByTitleContainsIgnoreCase(titleSubstring);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blogpost> getPostById(@PathVariable Long id){
        return this.bpr.findById(id)
                .map(blogpost -> new ResponseEntity<>(blogpost, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createPost(@RequestBody Blogpost blogpost){
        bpr.save(blogpost);
    }

    @PostMapping(value = "/{id}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createCommentForPost(@PathVariable Long id, @RequestBody Comment comment){
        var bpOptional = bpr.findById(id);
        if(bpOptional.isPresent()) {
            var bp = bpOptional.get();
            bp.getComments().add(comment);
            bpr.save(bp);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id){
        bpr.deleteById(id);
    }

    @PutMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updatePost(@RequestBody Blogpost blogpost){
        var bpOptional = bpr.findById(blogpost.getId());
        if(bpOptional.isPresent()) {
            var bpToUpdate = bpOptional.get();
            bpToUpdate.setText(blogpost.getText());
            bpToUpdate.setTitle(blogpost.getTitle());
            bpr.save(bpToUpdate);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

@RestController
@RequestMapping("/api/comments")
class CommentsController {
    // ... TBD, depending on business requirements
}

@Repository
interface BlogpostRepository extends JpaRepository<Blogpost, Long> {
    List<Blogpost> findByTitleContainsIgnoreCase(String substring);

    @Query(value = "SELECT bp FROM Blogpost bp ORDER BY bp.text ASC")
    List<Blogpost> findAllPostsOrderByText();
}

@Repository
interface CommentRepository extends JpaRepository<Comment, Long> {}


@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
class Blogpost {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private @Column(nullable = false) String title;
    private @Column(nullable = false) String text;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="blogpost_id")
    private Set<Comment> comments;

    public Blogpost(String title, String text, Set<Comment> comments) {
        this.title = title;
        this.text = text;
        this.comments = comments;
    }
}


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;

    // TODO :: ignore property when serializing
    // @ManyToOne
    // @JoinColumn(name = "blogpost_id")
    // Blogpost blogPost;

    public Comment(String text) {
        this.text = text;
    }
}


@Service
class DbInit implements CommandLineRunner {
    @Autowired
    private BlogpostRepository bpr;

    @Autowired
    private CommentRepository cr;

    @Override
    public void run(String... args) throws Exception {
        cr.deleteAll();
        bpr.deleteAll();

        var c1 = new Comment("Comm1");
        var c2 = new Comment( "Comm2");
        var c3 = new Comment( "Comm3");

        var bp1 = new Blogpost("Title 1", "Some text 1", new HashSet<>(){{ add(c1); add(c2); }});
        var bp2 = new Blogpost("Title 2", "Some text 2", new HashSet<>(){{ add(c3); }});

        bpr.saveAll(Arrays.asList(bp1, bp2));
    }
}