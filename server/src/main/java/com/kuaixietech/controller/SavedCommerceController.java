package com.kuaixietech.controller;
import com.kuaixietech.service.account.impl.SavedCommerceService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/me")
public class SavedCommerceController {
 private final SavedCommerceService service;
 public SavedCommerceController(SavedCommerceService service){this.service=service;}
 @GetMapping("/addresses") public List<Map<String,Object>> addresses(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.addresses(limit,offset);}
 @PostMapping("/addresses") public Map<String,String> createAddress(@Valid @RequestBody SavedCommerceService.AddressInput input){return service.saveAddress(null,input);}
 @PutMapping("/addresses/{id}") public Map<String,String> address(@PathVariable String id,@Valid @RequestBody SavedCommerceService.AddressInput input){return service.saveAddress(id,input);}
 @DeleteMapping("/addresses/{id}") public void deleteAddress(@PathVariable String id){service.deleteAddress(id);}
 @GetMapping("/cart") public List<Map<String,Object>> cart(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.cart(limit,offset);}
 @PutMapping("/cart/{product}") public void cart(@PathVariable String product,@Valid @RequestBody SavedCommerceService.CartInput input){service.cart(product,input);}
 @DeleteMapping("/cart/{product}") public void deleteCart(@PathVariable String product){service.deleteCart(product);}
 @GetMapping("/favorites") public List<Map<String,Object>> favorites(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.favorites(limit,offset);}
 @PutMapping("/favorites/{type}/{target}") public void favorite(@PathVariable String type,@PathVariable String target){service.favorite(type,target);}
 @DeleteMapping("/favorites/{type}/{target}") public void deleteFavorite(@PathVariable String type,@PathVariable String target){service.deleteFavorite(type,target);}
}
