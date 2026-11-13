package com.ruoyi.project.face.controller;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.service.IFaceEngineService;
import com.ruoyi.project.face.service.ICfStudentService;
import com.ruoyi.project.system.domain.SysDept;
import com.ruoyi.project.system.service.ISysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 学生管理Controller
 *
 * @author ruoyi
 * @date 2024-10-17
 */
@RestController
@RequestMapping("/face/student")
public class CfStudentController extends BaseController {
    @Autowired
    private ICfStudentService cfStudentService;
    @Autowired
    private IFaceEngineService faceEngineService;
    @Autowired
    private ISysDeptService deptService;

    /**
     * 查询学生管理列表
     */
    @PreAuthorize("@ss.hasPermi('face:student:list')")
    @GetMapping("/list")
    public TableDataInfo list(CfStudent cfStudent) {
        startPage();
        List<CfStudent> list = cfStudentService.selectCfStudentList(cfStudent);
        return getDataTable(list);
    }

    /**
     * 归属校区树（教师端新增/编辑学生用）
     * 不做数据权限过滤，保证能看到全部校区（市南/市北等）
     */
    @PreAuthorize("@ss.hasAnyPermi('face:student:list,face:student:query,face:student:add,face:student:edit')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDept dept) {
        return success(deptService.selectDeptTreeListAll(dept));
    }

    /**
     * 导出学生管理列表
     */
    @PreAuthorize("@ss.hasPermi('face:student:export')")
    @Log(title = "学生管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CfStudent cfStudent) {
        List<CfStudent> list = cfStudentService.selectCfStudentList(cfStudent);
        ExcelUtil<CfStudent> util = new ExcelUtil<CfStudent>(CfStudent.class);
        util.exportExcel(response, list, "学生管理数据");
    }

    /**
     * 获取学生管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('face:student:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cfStudentService.selectCfStudentById(id));
    }

    /**
     * 新增学生管理
     */
    @PreAuthorize("@ss.hasPermi('face:student:add')")
    @Log(title = "学生管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CfStudent cfStudent) {
        CfStudent saved = cfStudentService.insertCfStudent(cfStudent);
        return AjaxResult.success("保存成功，请将绑定码告知家长", saved);
    }

    /**
     * 修改学生管理
     */
    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CfStudent cfStudent) {
        return success(cfStudentService.updateCfStudent(cfStudent));
    }

    /**
     * 重新生成绑定码
     */
    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/regenerate-bind-code")
    public AjaxResult regenerateBindCode(@PathVariable Long id) {
        return success(cfStudentService.regenerateBindCode(id));
    }

    /**
     * 同步学生照片到百度云人脸库
     */
    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PostMapping("/sync-baidu-faces")
    public AjaxResult syncBaiduFaces() {
        return success(faceEngineService.syncAllStudentFaces());
    }

    /**
     * 清理本地人脸照片：保留库中在用的一张，删除多余与无引用文件
     */
    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.CLEAN)
    @PostMapping("/cleanup-local-faces")
    public AjaxResult cleanupLocalFaces() {
        return success(cfStudentService.cleanupLocalFaces());
    }

    /**
     * 同步单个学生照片到人脸引擎（按配置写百度/本地）
     */
    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/sync-baidu-face")
    public AjaxResult syncBaiduFace(@PathVariable Long id) {
        faceEngineService.registerStudentFaceFromAvatar(id);
        return success();
    }

    /**
     * 删除学生管理
     */
    @PreAuthorize("@ss.hasPermi('face:student:remove')")
    @Log(title = "学生管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cfStudentService.deleteCfStudentByIds(ids));
    }


    /**
     * 下载学生照片
     */
    @PreAuthorize("@ss.hasPermi('face:student:list')")
    @Log(title = "学生管理", businessType = BusinessType.EXPORT)
    @GetMapping("/downloadPhotos")
    public ResponseEntity<Resource> downloadPhotos() throws IOException {
        List<String> filenames = cfStudentService.selectAvatars();

        String localPath = RuoYiConfig.getProfile();

        // 创建临时zip文件
        File zipFile = File.createTempFile("images", ".zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String filename : filenames) {
                if (StringUtils.isEmpty(filename)) {
                    continue;
                }
                File imageFile = new File(localPath + StringUtils.substringAfter(filename, Constants.RESOURCE_PREFIX));

                if (imageFile.exists()) {
                    // 添加文件到zip
                    ZipEntry zipEntry = new ZipEntry(imageFile.getName());
                    zos.putNextEntry(zipEntry);
                    byte[] bytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
                    zos.write(bytes, 0, bytes.length);
                    zos.closeEntry();
                }
            }
        }

        // 准备下载
        Path path = Paths.get(zipFile.getAbsolutePath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"images.zip\"")
                .body(resource);
    }
}
