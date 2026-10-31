package com.kuaixietech.entity.communication;
import jakarta.validation.constraints.*;
public final class SupportDtos{
 private SupportDtos(){}
 public record Send(@NotBlank @Size(max=2000) String content){}
 public record AgentReply(@NotBlank @Size(max=2000) String content){}
}
