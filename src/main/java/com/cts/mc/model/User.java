package com.cts.mc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bharatkumar
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

	private String firstName;
	private String lastName;
	private String mobileNo;
	private String emailId;
	private String dateOfBirth;
	private String permamentAccessCode;

}
