package com.kuaixietech.service.trading.impl;

import com.kuaixietech.mapper.trading.DemandReleaseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Map;
import java.util.UUID;

/**
 * Port of public.reveal_due_demands(). The original handles all due rows, not just 500.
 * Each bounded FOR UPDATE SKIP LOCKED batch commits independently to avoid long locks.
 * Concurrent schedulers can work without selecting the same rows.
 */
@Service
public class DemandReleaseService {
    static final int BATCH_SIZE = 500;
    private final DemandReleaseMapper mapper;
    private final TransactionTemplate transactions;

    public DemandReleaseService(DemandReleaseMapper mapper, TransactionTemplate transactions) {
        this.mapper = mapper;
        this.transactions = transactions;
    }

    public int revealDueDemands() {
        int processed = 0;
        for (;;) {
            Integer batch = transactions.execute(status -> revealSingleBatch());
            if (batch == null) throw new IllegalStateException("No transaction result for due-demand release");
            processed = Math.addExact(processed, batch);
            if (batch < BATCH_SIZE) return processed;
        }
    }

    private int revealSingleBatch() {
        int count = 0;
        for (Map<String,Object> demand : mapper.lockDueDemands(BATCH_SIZE)) {
            String id = String.valueOf(demand.get("id"));
            if (mapper.clearScheduledAt(id) != 1) continue;
            boolean visible = "public".equals(String.valueOf(demand.get("visibility")));
            String name = String.valueOf(demand.get("title"));
            String title = visible ? "你的预约单已上线" : "隐身预约已进入候补";
            String body = visible
                ? "『" + name + "』已到预约时间，现已公开征集带单人报价。"
                : "『" + name + "』已到预约时间，正隐身等待顺路行程，出现匹配时自动上线。";
            mapper.insertReleaseNotification(UUID.randomUUID().toString(),
                String.valueOf(demand.get("publisher_id")), title, body);
            count++;
        }
        return count;
    }
}
