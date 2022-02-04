package com.hta.lecture.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hta.lecture.dto.WishlistDto;
import com.hta.lecture.mapper.WishlistMapper;
import com.hta.lecture.vo.Wishlist;

@Service
@Transactional
public class WishlistService {

	@Autowired
	private WishlistMapper wishlistMapper;
	
	public List<WishlistDto> getWishListByUserNo(int no){
		List<WishlistDto> wishList = wishlistMapper.getWishClasstByUserNo(no);
		return wishList;
	}
	public Wishlist getWishByUserNoClassNo(Wishlist wishlist){
		Wishlist wish = wishlistMapper.getWishByUserNoClassNo(wishlist);
		return wish;
	}
	
	public void deleteItem(int no) {
		wishlistMapper.deleteWishListByNo(no);
	}
	
	public void addWishList(Wishlist wishlist) {
		Wishlist wish = wishlistMapper.getWishByUserNoClassNo(wishlist);
		// classNo와 userNo로 wishlist테이블에서 동일 상품 유무조회
		// 있으면 RuntimeException throw 아니면 저장.	
		if(wish != null) {
			throw new RuntimeException("이미 위시리스트에 담긴 강의입니다.");
		}
		wishlistMapper.addWishList(wishlist);
	}

}
 