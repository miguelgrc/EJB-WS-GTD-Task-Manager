package com.sdi.business.services;

import java.util.List;

import com.sdi.business.exception.BusinessException;
import com.sdi.dto.User;
import com.sdi.dto.UserInfo;

public interface AdminService {

	public void deepDeleteUser(Long id) throws BusinessException;

	public void disableUser(Long id) throws BusinessException;

	public void enableUser(Long id) throws BusinessException;

	public List<User> findAllUsers() throws BusinessException;

	public User findUserById(Long id) throws BusinessException;

	
	//Nuevo servicio
	public List<UserInfo> findAllUsersInfo() throws BusinessException;
}