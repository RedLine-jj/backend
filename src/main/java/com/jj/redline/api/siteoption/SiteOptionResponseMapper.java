package com.jj.redline.api.siteoption;

import com.jj.redline.domain.dto.siteoption.SiteOptionDetailResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionLogResponse;
import com.jj.redline.domain.dto.siteoption.SiteOptionResponse;
import com.jj.redline.domain.entity.SiteOption;
import com.jj.redline.domain.entity.SiteOptionLog;
import org.springframework.stereotype.Component;

@Component
public class SiteOptionResponseMapper {

    public SiteOptionResponse toResponse(SiteOption siteOption) {
        return new SiteOptionResponse(
                siteOption.getId(),
                siteOption.getSite().getSiteName(),
                siteOption.getModel().getModelName(),
                siteOption.getOptionLabel(),
                siteOption.getPrice(),
                siteOption.getStatus(),
                siteOption.getLastCapturedAt()
        );
    }

    public SiteOptionDetailResponse toDetailResponse(SiteOption siteOption) {
        return new SiteOptionDetailResponse(
                siteOption.getId(),
                siteOption.getSite().getSiteName(),
                siteOption.getSite().getSiteLink(),
                siteOption.getModel().getBrand().getBrandName(),
                siteOption.getModel().getModelName(),
                siteOption.getModel().getImageUrl(),
                siteOption.getOptionLabel(),
                siteOption.getPrice(),
                siteOption.getStatus(),
                siteOption.getUrl(),
                siteOption.getLastCapturedAt()
        );
    }

    public SiteOptionLogResponse toLogResponse(SiteOptionLog siteOptionLog) {
        return new SiteOptionLogResponse(
                siteOptionLog.getId(),
                siteOptionLog.getOptionLabel(),
                siteOptionLog.getPrice(),
                siteOptionLog.getStatus(),
                siteOptionLog.getCapturedAt()
        );
    }
}
