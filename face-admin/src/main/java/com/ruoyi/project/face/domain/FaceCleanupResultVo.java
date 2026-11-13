package com.ruoyi.project.face.domain;

import lombok.Data;

/**
 * Local face cleanup result.
 */
@Data
public class FaceCleanupResultVo {

    /** scanned files under face/ */
    private int scannedFiles;

    /** kept in-use faces */
    private int kept;

    /** deleted extras/orphans */
    private int deleted;

    /** avatar path missing and unrestorable */
    private int missing;

    /** avatar repaired from leftover face files */
    private int repaired;
}
