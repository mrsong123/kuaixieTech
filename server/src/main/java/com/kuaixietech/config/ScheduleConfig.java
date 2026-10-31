package com.kuaixietech.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables the six separate, configuration-controlled scheduled jobs in
 * com.kuaixietech.scheduler. No IOrderService dependency is required.
 * No scheduled function executes unless its own property is enabled.
 */
@Configuration
@EnableScheduling
public class ScheduleConfig { }
