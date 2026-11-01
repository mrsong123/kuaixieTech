package com.kuaixietech.storage;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/me/uploads")
public class UploadController {
 private final PrivateUploads uploads;
 public UploadController(PrivateUploads uploads){this.uploads=uploads;}
 @PostMapping(value="",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Map<String,Object> upload(@RequestPart("file") MultipartFile file,@RequestParam String purpose){return uploads.upload(file,purpose);}
 @GetMapping public List<Map<String,Object>> mine(@RequestParam String purpose){return uploads.mine(purpose);}
 @DeleteMapping("/{id}") public Map<String,String> remove(@PathVariable String id){return uploads.remove(id);}
 @GetMapping("/deletions") public List<Map<String,Object>> deletions(){return uploads.deletions();}
 @GetMapping("/{id}/content") public ResponseEntity<byte[]> content(@PathVariable String id){var document=uploads.download(id);return ResponseEntity.ok().contentType(MediaType.parseMediaType(document.contentType())).header("Content-Disposition","attachment; filename=attachment").header("X-Content-Type-Options","nosniff").header("Cache-Control","private, no-store").body(document.bytes());}
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<ProblemDetail> invalid(){return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"UPLOAD_INPUT_REJECTED"));}
 @ExceptionHandler({IllegalStateException.class,DataAccessException.class}) public ResponseEntity<ProblemDetail> unavailable(){return ResponseEntity.status(503).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"UPLOAD_SERVICE_UNAVAILABLE"));}
}
