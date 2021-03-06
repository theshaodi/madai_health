package site.madai.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.madai.constant.MessageConstant;
import site.madai.entity.QueryPageBean;
import site.madai.entity.Result;
import site.madai.service.MemberService;
import site.madai.service.ReportService;
import site.madai.service.SetmealService;
import site.madai.utils.DateUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Project: site.madai.controller
 * @Author: ShaoDi Wang
 * @Date: Created in 2019-07-01 17:07
 * @Description:
 * @Version: 1.0
 */
@RestController
@RequestMapping("report")
public class ReportController {
    @Reference
    private MemberService memberService;

    @Reference
    private SetmealService setmealService;

    @Reference
    private ReportService reportService;

    /**
     * 获取运营统计数据
     *
     * @return
     */
    @RequestMapping("getBusinessReportData")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public Result getBusinessReportData() {
        try {
            Map<String, Object> result = reportService.getBusinessReport();
            return new Result(true, MessageConstant.GET_BUSINESS_REPORT_SUCCESS, result);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }

    /**
     * 套餐占比统计
     *
     * @return
     */
    @RequestMapping("getSetmealReport")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public Result getSetmealReport() {
        List<Map<String, Object>> list = setmealService.findSetmealCount();

        Map<String, Object> map = new HashMap<>();
        map.put("setmealCount", list);

        List<String> setmealNames = new ArrayList<>();
        for (Map<String, Object> m : list) {
            String name = (String) m.get("name");
            setmealNames.add(name);
        }
        map.put("setmealNames", setmealNames);

        return new Result(true, MessageConstant.GET_SETMEAL_COUNT_REPORT_SUCCESS, map);
    }

    /**
     * 会员数量统计
     *
     * @return
     */
    @RequestMapping("getMemberReport")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public Result getMemberReport(@RequestBody QueryPageBean queryPageBean) {
        // 如果前端传来了日期，则用前端传来的日期
        List<String> list = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        if (queryPageBean.getDateRangeList() != null && queryPageBean.getDateRangeList().size() == 2){
            String minDate =
                    simpleDateFormat.format(queryPageBean.getDateRangeList().get(0));
            String maxDate =
                    simpleDateFormat.format(queryPageBean.getDateRangeList().get(1));
            try {
                list = DateUtils.getMonthBetween(minDate, maxDate, "yyyy.MM");
            } catch (Exception e) {
                return new Result(false,e.getMessage());
            }

        }else{
            Calendar calendar = Calendar.getInstance();
            //获得当前日期之前12个月的日期
            calendar.add(Calendar.MONTH, -12);

            for (int i = 0; i < 12; i++) {
                calendar.add(Calendar.MONTH, 1);
                list.add(new SimpleDateFormat("yyyy.MM").format(calendar.getTime()));
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("months", list);

        List<Integer> memberCount = memberService.findMemberCountByMonth(list);
        map.put("memberCount", memberCount);

        return new Result(true, MessageConstant.GET_MEMBER_NUMBER_REPORT_SUCCESS, map);
    }

    /**
     * 导出Excel报表
     *
     * @return
     */
    @RequestMapping("exportBusinessReport")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public Result exportBusinessReport(HttpServletRequest request, HttpServletResponse response) {
        try {
            //远程调用报表服务获取报表数据
            Map<String, Object> result = reportService.getBusinessReport();

            //取出返回结果数据，准备将报表数据写入到Excel文件中
            String reportDate = (String) result.get("reportDate");
            Integer todayNewMember = (Integer) result.get("todayNewMember");
            Integer totalMember = (Integer) result.get("totalMember");
            Integer thisWeekNewMember = (Integer) result.get("thisWeekNewMember");
            Integer thisMonthNewMember = (Integer) result.get("thisMonthNewMember");
            Integer todayOrderNumber = (Integer) result.get("todayOrderNumber");
            Integer thisWeekOrderNumber = (Integer) result.get("thisWeekOrderNumber");
            Integer thisMonthOrderNumber = (Integer) result.get("thisMonthOrderNumber");
            Integer todayVisitsNumber = (Integer) result.get("todayVisitsNumber");
            Integer thisWeekVisitsNumber = (Integer) result.get("thisWeekVisitsNumber");
            Integer thisMonthVisitsNumber = (Integer) result.get("thisMonthVisitsNumber");
            List<Map> hotSetmeal = (List<Map>) result.get("hotSetmeal");

            //获得Excel模板文件绝对路径
            String temlateRealPath = request.getSession().getServletContext().getRealPath("template") +
                    File.separator + "report_template.xlsx";

            //读取模板文件创建Excel表格对象
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(temlateRealPath)));
            XSSFSheet sheet = workbook.getSheetAt(0);

            XSSFRow row = sheet.getRow(2);
            //日期
            row.getCell(5).setCellValue(reportDate);

            row = sheet.getRow(4);
            //新增会员数（本日）
            row.getCell(5).setCellValue(todayNewMember);
            //总会员数
            row.getCell(7).setCellValue(totalMember);

            row = sheet.getRow(5);
            //本周新增会员数
            row.getCell(5).setCellValue(thisWeekNewMember);
            //本月新增会员数
            row.getCell(7).setCellValue(thisMonthNewMember);

            row = sheet.getRow(7);
            //今日预约数
            row.getCell(5).setCellValue(todayOrderNumber);
            //今日到诊数
            row.getCell(7).setCellValue(todayVisitsNumber);

            row = sheet.getRow(8);
            //本周预约数
            row.getCell(5).setCellValue(thisWeekOrderNumber);
            //本周到诊数
            row.getCell(7).setCellValue(thisWeekVisitsNumber);

            row = sheet.getRow(9);
            //本月预约数
            row.getCell(5).setCellValue(thisMonthOrderNumber);
            //本月到诊数
            row.getCell(7).setCellValue(thisMonthVisitsNumber);

            int rowNum = 12;
            //热门套餐
            for (Map map : hotSetmeal) {
                String name = (String) map.get("name");
                Long setmeal_count = (Long) map.get("setmeal_count");
                BigDecimal proportion = (BigDecimal) map.get("proportion");
                row = sheet.getRow(rowNum++);
                //套餐名称
                row.getCell(4).setCellValue(name);
                //预约数量
                row.getCell(5).setCellValue(setmeal_count);
                //占比
                row.getCell(6).setCellValue(proportion.doubleValue());
            }

            //通过输出流进行文件下载
            ServletOutputStream out = response.getOutputStream();
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("content-Disposition", "attachment;filename=report.xlsx");
            workbook.write(out);

            out.flush();
            out.close();
            workbook.close();

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.GET_BUSINESS_REPORT_FAIL, null);
        }
    }
}
