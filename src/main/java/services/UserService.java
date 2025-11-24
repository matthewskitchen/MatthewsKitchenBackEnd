package services;

import dbmodel.users;

public interface UserService {
	Response registerUser(users user);
    Response loginUser(String email, String password);
}
