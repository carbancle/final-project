package com.hta.lecture.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hta.lecture.service.ClassService;
import com.hta.lecture.vo.Classes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/instructor")
public class TeacherController {
static final Logger logger = LogManager.getLogger(ClassController.class);
	
	@Autowired
	ClassService classService;
	
	// 상세페이지 이동
	@GetMapping("/{no}")
	public String detail(@PathVariable(name = "no") int no, Model model){
		
		log.info("조회할 강의번호: " + no);
		Classes classes = classService.getClassDetail(no);
		
		model.addAttribute("classes", classes);
		
		return "/teacher-mypage/home/dashboard";
	}
}
