package com.hta.lecture.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hta.lecture.dto.ClassCourseDto;
import com.hta.lecture.dto.ClassPagination;
import com.hta.lecture.service.ClassService;
import com.hta.lecture.vo.Category;
import com.hta.lecture.vo.ClassFiles;
import com.hta.lecture.vo.Classes;
import com.hta.lecture.web.form.ClassCriteria;
import com.hta.lecture.web.form.ClassInsertForm;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/course")
public class ClassController {
	
	static final Logger logger = LogManager.getLogger(ClassController.class);
	
	@Autowired
	ClassService classService;
	
	@GetMapping
	public String list(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			ClassCriteria criteria, Model model) {

		logger.info("요청 페이지번호 : " + page);
		logger.info("검색조건 및 값 :" + criteria);
		
		if (!StringUtils.hasText(criteria.getCategory())) {
			criteria.setCategory(null);
		}
		
		// 검색조건에 해당하는 총 데이터 갯수 조회
		int totalRecords = classService.getTotalRows(criteria);
		// 현재 페이지번호와 총 데이터 갯수를 전달해서 페이징 처리에 필요한 정보를 제공하는 Pagination객체 생성
		ClassPagination pagination = new ClassPagination(page, totalRecords);
		
		// 요청한 페이지에 대한 조회범위를 criteria에 저장
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);

		// 검색조건(value)과 조회범위(beginIndex, endIndex)가 포함된 Criteria를 서비스에 전달해서 데이터 조회
		List<ClassCourseDto> classes = classService.getAllCourseInfo(criteria);
		List<Category> categoryList = classService.getAllClassCategories();
		
		model.addAttribute("classes", classes);		
		model.addAttribute("categoryList", categoryList);		
		model.addAttribute("pagination", pagination);
		
		return "/courses/list"; // list.jsp
	}
	
	
	
	// 상세페이지 이동
	@GetMapping("/{no}")
	public String detail(@PathVariable(name = "no") int no, Model model){
		
		log.info("조회할 강의번호: " + no);
		Classes classes = classService.getClassDetail(no);
		
		model.addAttribute("classes", classes);
		
		return "/courses/detail";
	}
	
	@GetMapping("/insert.do")
	public String insert() {
		
		return "class/insertForm";
	}
	
	@PostMapping("/insert.do")
	public String save(ClassInsertForm form) throws IOException{
		String saveDirectory = "C:\\Users\\HOME\\git\\final-project\\src\\main\\webapp\\resources\\image\\course";
		
		List<ClassFiles> classFiles = new ArrayList<ClassFiles>();
		
		List<MultipartFile> uploadFiles = form.getUploadFiles();
		for(MultipartFile multipartFile : uploadFiles) {
			if(!multipartFile.isEmpty()) {
				String filename = System.currentTimeMillis() + System.currentTimeMillis() + multipartFile.getOriginalFilename();
				
				ClassFiles classFile = new ClassFiles();
				classFile.setUploadFiles(filename);
				classFiles.add(classFile);
				
				InputStream in = multipartFile.getInputStream();
				FileOutputStream out = new FileOutputStream(new File(saveDirectory, filename));
				FileCopyUtils.copy(in, out);
			
			}
			
		}
		
		Classes classes = new Classes();
		BeanUtils.copyProperties(form, classes);
		classService.addNewClass(classes, classFiles);
		
		return "redirect:courses/list";
	}
}
