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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.hta.lecture.dto.ClassCourseDto;
import com.hta.lecture.dto.ClassPagination;
import com.hta.lecture.dto.ClassesDto;
import com.hta.lecture.service.ClassService;
import com.hta.lecture.service.ProgressService;
import com.hta.lecture.utils.SessionUtils;
import com.hta.lecture.vo.Category;
import com.hta.lecture.vo.ClassChapter;
import com.hta.lecture.vo.ClassDetail;
import com.hta.lecture.vo.ClassFiles;
import com.hta.lecture.vo.Classes;
import com.hta.lecture.vo.Progress;
import com.hta.lecture.vo.User;
import com.hta.lecture.web.form.ClassCriteria;
import com.hta.lecture.web.form.ClassInsertForm;
import com.hta.lecture.web.form.CurriculumForm;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/course")
@SessionAttributes("classNo")
public class ClassController {
	
	static final Logger logger = LogManager.getLogger(ClassController.class);
	
	@Autowired
	ClassService classService;

	@Autowired
	ProgressService progressService;
	
	@GetMapping
	public String list(@RequestParam(name = "page", required = false, defaultValue = "1") int page,
			ClassCriteria criteria, Model model) {

		logger.info("?????? ??????????????? : " + page);
		logger.info("???????????? ??? ??? :" + criteria);
		
		if (!StringUtils.hasText(criteria.getCategory())) {
			criteria.setCategory(null);
		}
		
		// ??????????????? ???????????? ??? ????????? ?????? ??????
		int totalRecords = classService.getTotalRows(criteria);
		// ?????? ?????????????????? ??? ????????? ????????? ???????????? ????????? ????????? ????????? ????????? ???????????? Pagination?????? ??????
		ClassPagination pagination = new ClassPagination(page, totalRecords);
		
		// ????????? ???????????? ?????? ??????????????? criteria??? ??????
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("???????????? ??? ??? :" + criteria);

		// ????????????(value)??? ????????????(beginIndex, endIndex)??? ????????? Criteria??? ???????????? ???????????? ????????? ??????
		List<ClassCourseDto> classes = classService.getAllCourseInfo(criteria);
		List<Category> categoryList = classService.getAllClassCategories();
		
		model.addAttribute("classes", classes);		
		model.addAttribute("categoryList", categoryList);		
		model.addAttribute("pagination", pagination);
		
		return "/courses/list"; // list.jsp
	}
	
	// ??????????????? ??????
	@GetMapping("/{no}")
	public String detail(@PathVariable(name = "no") int no, Model model){
		
		log.info("????????? ????????????: " + no);
		ClassesDto classes = classService.getClassDetail(no);
		

		User user = (User)SessionUtils.getAttribute("LOGIN_USER");
		Progress savedProgress = null;
		if(user != null) {
			Progress progress = Progress.builder().classNo(no).userNo(user.getNo()).build();
			savedProgress = progressService.checkProgressByUserNoClassNo(progress);
			log.info("???????????? ????????????:",progress);
			log.info("???????????????:",savedProgress);
		}
		model.addAttribute("savedProgress", savedProgress);

		model.addAttribute("classes", classes);
		
		return "/courses/detail";
	}
	
	@PostMapping("/insert-progress")
	public String insertProgress(@RequestParam(name = "no") int no){
		
		log.info("??????????????? ????????? ????????????: " + no);
		ClassesDto classes = classService.getClassDetail(no);
		
		User user = (User)SessionUtils.getAttribute("LOGIN_USER");
		
		if(user != null) {
			Progress progress = new Progress();
			progress.setClassNo(classes.getNo());
			progress.setUserNo(user.getNo());
			
			progressService.insertPrgoressByUserNoClassNo(progress);
		}
		
		return "redirect:/course/" + no;
	}

	@GetMapping("/insert.do")
	public String insert(@RequestParam(name = "no") int no, Model model) {
		
		int teacherNo = classService.getTeacherNoByUserNo(no);
		model.addAttribute("teacherNo", teacherNo);
		
		return "courses/insertForm";
	}
	
	@PostMapping("/insert.do")
	public String save(ClassInsertForm form) throws IOException{
		String saveDirectory = "C:\\Users\\HOME\\git\\final-project\\src\\main\\webapp\\resources\\images\\course";
		
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
		
		return "redirect:insertDetail.do?no=" + classes.getNo();
	}
	
		@GetMapping("/insertDetail.do")
		public String insertDetail(@RequestParam(name = "no") int no, Model model) {

			model.addAttribute("no", no);
			
	      return "courses/CurriculumForm";
	   }
	   
	   @PostMapping("/insertDetail.do")
	   public String saveDetail(CurriculumForm formDetail) {
	      
	      User user = (User)SessionUtils.getAttribute("LOGIN_USER");
	      
	      ClassChapter chapter = new ClassChapter();
			ClassDetail detail = new ClassDetail();
			
			BeanUtils.copyProperties(formDetail, chapter);
			BeanUtils.copyProperties(formDetail, detail);
			classService.addNewChapter(chapter);
			detail.setChapterNo(chapter.getChapterNo());
			classService.addNewDetail(detail);
	      
			
	      return "redirect:/instructor/" + user.getNo();
	   }
}
