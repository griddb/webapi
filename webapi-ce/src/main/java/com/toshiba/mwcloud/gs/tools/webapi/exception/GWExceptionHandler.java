/*
 	Copyright (c) 2019 TOSHIBA Digital Solutions Corporation.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.toshiba.mwcloud.gs.tools.webapi.exception;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.common.GSStatementException;
import com.toshiba.mwcloud.gs.tools.webapi.dto.GWError;

@ControllerAdvice
public class GWExceptionHandler {

	/**
	 * Constructor for {@link GWError}
	 */
	@Autowired
	private GWError error;

	/**
	 * Handle other internal server exception
	 * 
	 * @param e
	 *            a {@link Exception} exception
	 * @return a {@link ResponseEntity} object with
	 *         {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 */
	@ExceptionHandler({ Exception.class })
	@ResponseBody
	public ResponseEntity<?> handleRequestException(Exception e) {
		error.setErrorMessage(e.getMessage());
		error.setErrorCode(0);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Handle GWBadRequestException
	 * 
	 * @param e
	 *            a {@link GWBadRequestException} exception
	 * @return a {@link ResponseEntity} object with
	 *         {@link HttpStatus#BAD_REQUEST}
	 */
	@ExceptionHandler({ GWBadRequestException.class, MethodArgumentTypeMismatchException.class,
			ClassCastException.class, IOException.class })
	@ResponseBody
	public ResponseEntity<?> handleGWBadRequestException(Exception e) {

		error.setErrorMessage(e.getMessage());
		error.setErrorCode(0);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle GWUnauthorizedException
	 * 
	 * @param e
	 *            a {@link GWUnauthorizedException} exception
	 * @return {@link ResponseEntity} object with
	 *         {@link HttpStatus#UNAUTHORIZED}
	 */
	@ExceptionHandler({ GWUnauthorizedException.class })
	@ResponseBody
	public ResponseEntity<?> handleGWUnauthorizedException(GWUnauthorizedException e) {

		error.setErrorMessage(e.getMessage());
		error.setErrorCode(0);
		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Handle GWNotFoundException
	 * 
	 * @param e
	 *            a {@link GWNotFoundException} exception
	 * @return {@link ResponseEntity} object with {@link HttpStatus#NOT_FOUND}
	 */
	@ExceptionHandler({ GWNotFoundException.class })
	@ResponseBody
	public ResponseEntity<?> handleGWNotFoundException(GWNotFoundException e) {

		error.setErrorMessage(e.getMessage());
		error.setErrorCode(0);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	/**
	 * Handle GWResourceConflictedException
	 * 
	 * @param e
	 *            a {@link GWResourceConflictedException} exception
	 * @return {@link ResponseEntity} object with {@link HttpStatus#CONFLICT}
	 */
	@ExceptionHandler({ GWResourceConflictedException.class })
	@ResponseBody
	public ResponseEntity<?> handleGWResourceConflictedException(GWResourceConflictedException e) {
		error.setErrorMessage(e.getMessage());
		error.setErrorCode(0);
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	/**
	 * Handle HttpMediaTypeNotSupportedException
	 * 
	 * @param e
	 *            a {@link HttpMediaTypeNotSupportedException} exception
	 * @return {@link ResponseEntity} object with
	 *         {@link HttpStatus#UNSUPPORTED_MEDIA_TYPE}
	 */
	@ExceptionHandler({ HttpMediaTypeNotSupportedException.class })
	@ResponseBody
	public ResponseEntity<?> handleGWHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
		error.setErrorMessage(e.getMessage());
		error.setErrorCode(0);
		return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	/**
	 * Handle HttpRequestMethodNotSupportedException
	 * 
	 * @param e
	 *            a {@link HttpRequestMethodNotSupportedException} exception
	 * @return {@link ResponseEntity} object with
	 *         {@link HttpStatus#METHOD_NOT_ALLOWED}
	 */
	@ExceptionHandler({ HttpRequestMethodNotSupportedException.class })
	@ResponseBody
	public ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {

		error.setErrorMessage(e.getMessage());
		error.setErrorCode(0);
		return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
	}

	/**
	 * Handle parse JSON data
	 * 
	 * @param e
	 *            a {@link HttpMessageNotReadableException} exception
	 * @return a {@link ResponseEntity} object with
	 *         {@link HttpStatus#BAD_REQUEST}
	 */
	@ExceptionHandler({ HttpMessageNotReadableException.class })
	@ResponseBody
	public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		error.setErrorCode(0);
		String message = "";
		if (e.getCause() instanceof JsonMappingException) {
			JsonMappingException jsonMappingException = (JsonMappingException) e.getCause();
			List<Reference> listReference = jsonMappingException.getPath();
			if (listReference.size() == 0) {
				message = jsonMappingException.getMessage();
			} else {
				message = "Mapping JSON data error at ";
				for (Reference reference : jsonMappingException.getPath()) {
					if (reference.getFieldName() == null) {
						message += " object " + (reference.getIndex() + 1);
					} else {
						message += " field '" + reference.getFieldName() + "'";
					}
				}
			}

		} else if (e.getCause() instanceof JsonParseException) {
			JsonParseException jsonParseException = (JsonParseException) e.getCause();
			message = jsonParseException.getMessage();
		} else {
			message = e.getMessage();
		}
		error.setErrorMessage(message);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle some internal exceptions
	 * 
	 * @param e
	 *            UnsupportedEncodingException, GWException
	 * @return {@link ResponseEntity} object with
	 *         {@link HttpStatus#INTERNAL_SERVER_ERROR}
	 */
	@ExceptionHandler({ UnsupportedEncodingException.class, GWException.class, ParseException.class })
	@ResponseBody
	public ResponseEntity<?> handleOtherInternalExceptions(Exception e) {

		error.setErrorMessage(e.getMessage());
		error.setErrorCode(0);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Handle SQLException
	 * 
	 * @param e
	 *            a {@link SQLException}
	 * @return {@link ResponseEntity} object with {@link HttpStatus#BAD_REQUEST}
	 */
	@ExceptionHandler
	@ResponseBody
	public ResponseEntity<?> handleSQLException(SQLException e) {

		error.setErrorMessage(e.getMessage());
		error.setErrorCode(e.getErrorCode());
		e.getErrorCode();
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle GSException from GridDB
	 * 
	 * @param e
	 *            a GSException
	 * @return a {@link ResponseEntity} object
	 */
	@ExceptionHandler({ GSException.class })
	@ResponseBody
	public ResponseEntity<?> handleGSException(GSException e) {
		error.setErrorMessage(e.getMessage());
		int errCode = e.getErrorCode();
		error.setErrorCode(errCode);

		switch (errCode) {
		case 10005:
		case 10053:
			return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
		case 0:
		case 1007:
		case 1008:
		case 60081:
		case 145001:
		case 145024:
		case 145007:
		case 150010:
		case 150012:
		case 150020:
		case 151001:
		case 151002:
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
		default:
			return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * handle GSStatement Exception.
	 *
	 * @param e GSStatementException
	 * @return HttpStatus
	*/
	@ExceptionHandler({ GSStatementException.class })
	@ResponseBody
	public ResponseEntity<?> handleGSStatementException(GSStatementException e) {
		error.setErrorMessage(e.getMessage());
		int errCode = e.getErrorCode();
		error.setErrorCode(errCode);
		switch (errCode) {
		case 10005:
		case 10053:
		case 10100:
		case 10102:
			return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);

		default:
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
		}
	}
}
