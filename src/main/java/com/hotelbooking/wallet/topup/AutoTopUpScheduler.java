package com.hotelbooking.wallet.topup;

import com.hotelbooking.wallet.Wallet;
import com.hotelbooking.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs every 5 minutes to check enabled auto top-up wallets
 * whose balance has dropped below the configured threshold.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AutoTopUpScheduler {

    private final TopUpConfigRepository configRepository;
    private final TopUpService topUpService;

    /**
     * Scans every 5 minutes for wallets with auto top-up enabled
     * and balance below threshold.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scanWallets() {
        log.info("AutoTopUpScheduler: Starting wallet scan");

        List<TopUpConfig> enabledConfigs = configRepository.findAll().stream()
                .filter(TopUpConfig::getEnabled)
                .toList();

        int triggered = 0;
        int skipped = 0;

        for (TopUpConfig config : enabledConfigs) {
            try {
                topUpService.triggerAutoTopUpIfNeeded(config.getWallet().getWalletId());
                triggered++;
            } catch (Exception e) {
                skipped++;
                log.warn("AutoTopUpScheduler: Skipped wallet {} — {}",
                        config.getWallet().getWalletId(), e.getMessage());
            }
        }

        log.info("AutoTopUpScheduler: Scan complete. Triggered={}, Skipped={}, Total={}",
                triggered, skipped, enabledConfigs.size());
    }
}
