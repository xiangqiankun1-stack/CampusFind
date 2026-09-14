package com.xqk.campusfind.service;

import com.xqk.campusfind.common.Result;
import com.xqk.campusfind.entity.Claim;
import java.util.List;

public interface ClaimService {
    Result<String> applyClaim(Claim claim);
    List<Claim> getAllClaimsWithDetails();
    Result<String> reviewClaim(Long id, Integer status);
    List<Claim> getMyClaims(Long userId);
}