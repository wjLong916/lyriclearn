package com.lyriclearn.evidence.controller;

import com.lyriclearn.common.ApiResponse;
import com.lyriclearn.evidence.EvidenceService;
import com.lyriclearn.evidence.entity.LearningCredential;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 存证：SBT 铸造、凭证查询、手动窗口锚定（演示）。 */
@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    /** SBT 铸造：校验断言摘要后铸造不可转让凭证。 */
    @PostMapping("/mint")
    public ApiResponse<Map<String, Object>> mint(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        String claimHash = (String) body.get("claimHash");
        return ApiResponse.ok(evidenceService.mintSbt(userId, claimHash));
    }

    @GetMapping("/{credentialId}")
    public ApiResponse<Map<String, Object>> get(@PathVariable String credentialId) {
        LearningCredential c = evidenceService.getCredential(credentialId)
                .orElseThrow(() -> new com.lyriclearn.common.BizException(404, "凭证不存在"));
        return ApiResponse.ok(Map.of(
                "credentialId", c.getCredentialId(),
                "userId", c.getUserId(),
                "claimHash", c.getClaimHash(),
                "txHash", c.getTxHash() == null ? "" : c.getTxHash(),
                "status", c.getStatus(),
                "transferable", false,
                "mintedAt", c.getMintedAt() == null ? "" : c.getMintedAt().toString()));
    }

    /** 手动触发批量锚定（生产由窗口定时任务自动执行）。 */
    @PostMapping("/anchor")
    public ApiResponse<Map<String, Object>> anchor() {
        return ApiResponse.ok(evidenceService.anchorWindow());
    }
}
