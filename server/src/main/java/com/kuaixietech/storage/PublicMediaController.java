package com.kuaixietech.storage;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
public class PublicMediaController {
 private final PrivateUploads uploads;
 public PublicMediaController(PrivateUploads uploads){this.uploads=uploads;}
 @GetMapping("/api/public/media/{id}") public ResponseEntity<byte[]> image(@PathVariable String id){var image=uploads.publicImage(id);return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType())).header("Cache-Control","no-store").header("X-Content-Type-Options","nosniff").header("Content-Disposition","inline; filename=product-image").body(image.bytes());}
 @ExceptionHandler({IllegalStateException.class,DataAccessException.class}) public ResponseEntity<ProblemDetail> unavailable(){return ResponseEntity.status(503).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"UPLOAD_SERVICE_UNAVAILABLE"));}
}
