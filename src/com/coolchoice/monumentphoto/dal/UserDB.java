package com.coolchoice.monumentphoto.dal;

import java.util.List;

import com.coolchoice.monumentphoto.data.User;

public class UserDB {
	
	public static User loginUser(User user){
		User dbUser = null;
		user.IsActive = 1;
		setAllUsersDisabled();
		List<User> users = DB.dao(User.class).queryForEq(User.USER_NAME_COLUMN, user.UserName);
		if(users.size() > 0){
			dbUser = users.get(0);
			dbUser.UserName = user.UserName;
			dbUser.LName = user.LName;
			dbUser.MName = user.MName;
			dbUser.FName = user.FName;
			dbUser.OrgId = user.OrgId;
			dbUser.IsActive = 1;
			DB.dao(User.class).update(dbUser);
		} else {
			dbUser = user;
			DB.dao(User.class).create(dbUser);
		}
		return dbUser;
	}
	
	public static User getCurrentUser(){
		User activeUser = null;
		List<User> activeUsers = DB.dao(User.class).queryForEq("IsActive", 1);
		if(activeUsers.size() == 1){
			activeUser = activeUsers.get(0);
		}
		return activeUser;
	}
	
	public static void setAllUsersDisabled(){
		DB.db().execManualSQL("update user set IsActive = 0;");
	}

}
