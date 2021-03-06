package com.skilldistillery.mvcdesolatemidterm.controllers;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.skilldistillery.jpadesolatemidterm.entities.Event;
import com.skilldistillery.jpadesolatemidterm.entities.PasswordDTO;
import com.skilldistillery.jpadesolatemidterm.entities.User;
import com.skilldistillery.mvcdesolatemidterm.data.UserDAO;

@Controller
public class UserController {

	@Autowired
	private UserDAO userDao;

	// you have the session to check for this
	private boolean loggedIn;

	// user controllers follow

	@RequestMapping(path = "welcome.do")
	public ModelAndView loginView() {
		ModelAndView mv = new ModelAndView();
		User user = new User();
		mv.addObject("user", user);
		mv.setViewName("WEB-INF/loginView.jsp");
		return mv;
	}

	@RequestMapping(path = "login.do", method = RequestMethod.POST)
	public ModelAndView loginMethod(User user, HttpSession session, Errors error) {
		ModelAndView mv = new ModelAndView();
		User userLogin = userDao.findUserByUsername(user.getUserName());

		if (userLogin != null && userLogin.isStatus() == true) {
			if (userDao.passwordConfirmation(userLogin, user.getPassword())) {

				loggedIn = true;
				session.setAttribute("loggedIn", loggedIn);
				session.setAttribute("userCurrent", userLogin);
				List<Event> eventList = userDao.listAllEvents();
				session.setAttribute("events", eventList);
				mv.setViewName("WEB-INF/landingPage.jsp");
			} else {
				error.rejectValue("password", "error.password", "error message");
				mv.setViewName("WEB-INF/loginView.jsp");

			}

		} else {

			error.rejectValue("userName", "error.userName", "error message");
			mv.setViewName("WEB-INF/loginView.jsp");
		}

		return mv;
	}

	@RequestMapping(path = "logout.do", method = RequestMethod.GET)
	public ModelAndView logoutMethod(@ModelAttribute("userCurrent") User user, HttpSession session,
			RedirectAttributes flash) {
		ModelAndView mv = new ModelAndView();
		User userLogout = (User) session.getAttribute("userCurrent");

		if (userLogout != null) {
			loggedIn = false;
			flash.addFlashAttribute("logOut", userLogout.getUserName());
			mv.setViewName("redirect:welcome.do");
		}

		return mv;
	}

	@RequestMapping(path = "adminPage.do")
	public ModelAndView adminPageView(HttpSession session, User user) {
		ModelAndView mv = new ModelAndView();
		List<User> completeUserList = userDao.listAllUsers();
		List<Event> completeEventList = userDao.listAllEvents();
		mv.addObject("events", completeEventList);
		mv.addObject("completeUserList", completeUserList);
		mv.setViewName("WEB-INF/adminPage.jsp");
		return mv;
	}

	@RequestMapping(path = "landingPage.do")
	public ModelAndView landingPageView(HttpSession session) {
		ModelAndView mv = new ModelAndView();
		List<Event> eventList = userDao.listAllEvents();
		session.setAttribute("events", eventList);
		User user = (User) session.getAttribute("userCurrent");
		session.setAttribute("userCurrent", user);
		mv.setViewName("WEB-INF/landingPage.jsp");
		return mv;
	}

	@RequestMapping(path = "register.do")
	public ModelAndView registerMethodView() {
		ModelAndView mv = new ModelAndView();
		User user = new User();
		mv.addObject("user", user);
		mv.setViewName("WEB-INF/registerView.jsp");
		return mv;
	}

	@RequestMapping(path = "registration.do", method = RequestMethod.POST)
	public ModelAndView Registered(User user, Errors error, RedirectAttributes flash, HttpSession session) {
		ModelAndView mv = new ModelAndView();
		if (userDao.uniqueUsername(user.getUserName())) {
			userDao.create(user);
			boolean added = true;
			flash.addFlashAttribute("added", added);
			mv.setViewName("redirect:welcome.do");

		} else {
			error.rejectValue("userName", "error.userName", "error message");
			mv.setViewName("WEB-INF/registerView.jsp");
		}
		return mv;
	}

	// activate and deactivate users

	@RequestMapping(path = "deactivateUser.do", method = RequestMethod.POST)
	public ModelAndView deactivateUser(HttpSession session, int id) {
		ModelAndView mv = new ModelAndView();
		userDao.deactivateUser(id);
		List<User> completeUserList = userDao.listAllUsers();
		mv.addObject("completeUserList", completeUserList);
		session.setAttribute("user", id);
		mv.setViewName("redirect:adminPage.do");
		return mv;

	}

	@RequestMapping(path = "reactivateUser.do", method = RequestMethod.POST)
	public ModelAndView reactivateUser(HttpSession session, int id) {
		ModelAndView mv = new ModelAndView();
		userDao.reactivateUser(id);
		List<User> completeUserList = userDao.listAllUsers();
		mv.addObject("completeUserList", completeUserList);
		session.setAttribute("user", id);
		mv.setViewName("redirect:adminPage.do");
		return mv;

	}

	@RequestMapping(path = "deactivateAdmin.do", method = RequestMethod.POST)
	public ModelAndView deactivateAdmin(HttpSession session, int id) {
		ModelAndView mv = new ModelAndView();
		userDao.deactivateAdmin(id);
		List<User> completeUserList = userDao.listAllUsers();
		mv.addObject("completeUserList", completeUserList);
		session.setAttribute("user", id);
		mv.setViewName("redirect:adminPage.do");
		return mv;

	}

	@RequestMapping(path = "activateAdmin.do", method = RequestMethod.POST)
	public ModelAndView activateAdmin(HttpSession session, int id) {
		ModelAndView mv = new ModelAndView();
		userDao.activateAdmin(id);
		List<User> completeUserList = userDao.listAllUsers();
		mv.addObject("completeUserList", completeUserList);
		session.setAttribute("user", id);
		mv.setViewName("redirect:adminPage.do");
		return mv;

	}

	@RequestMapping(path = "changePassword.do", method = RequestMethod.POST)
	public ModelAndView changePassword(PasswordDTO passwordDTO, int id, Errors error, RedirectAttributes flash) {
		User checkUserPassword = userDao.findUserByUserID(id);
		ModelAndView mv = new ModelAndView();
		if (userDao.passwordConfirmation(checkUserPassword, passwordDTO.getOldPassword())) {
			if (userDao.setNewPassword(id, passwordDTO.getNewPassword())) {
				flash.addFlashAttribute("success", "Password Changed Successfully");
				mv.setViewName("redirect:profileView.do");
			} else {
				error.rejectValue("newPassword", "error.newPassword", "error message");
				mv.setViewName("WEB-INF/profilePage.jsp");

			}
		} else {
			error.rejectValue("oldPassword", "error.oldPassword", "error message");
			mv.setViewName("WEB-INF/profilePage.jsp");

		}

		return mv;

	}

	@RequestMapping(path = "setProfileBlurb.do")
	public ModelAndView setProfileBlurb(String blurb, int id, RedirectAttributes flash, HttpSession session) {
		ModelAndView mv = new ModelAndView();
		if (userDao.setProfileBlurb(id, blurb)) {
			flash.addFlashAttribute("SummaryUpdated", 7);
		} else {
			flash.addFlashAttribute("SummaryNotUpdated", 7);
		}

		mv.setViewName("redirect:profileView.do");
		return mv;
	}

	@RequestMapping(path = "setProfilePicture.do", method = RequestMethod.POST)
	public ModelAndView setProfilePicture(String picURL, int userId, HttpSession session, RedirectAttributes flash) {
		ModelAndView mv = new ModelAndView();
		if (userDao.setProfilePicture(userId, picURL)) {
			boolean updated = true;
			flash.addFlashAttribute("Profile Picture Updated", updated);
		} else {
			boolean updated = false;
			flash.addFlashAttribute("Profile Picture Remains the Same", updated);
		}
		mv.setViewName("redirect:profileView.do");
		return mv;
	}

}
