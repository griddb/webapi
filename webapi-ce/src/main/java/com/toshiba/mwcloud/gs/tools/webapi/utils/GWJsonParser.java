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

package com.toshiba.mwcloud.gs.tools.webapi.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class GWJsonParser extends JsonDeserializer<Object[][]> {

	@Override
	public Object[][] deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		List<Object[]> rowList = new ArrayList<Object[]>();
		if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
			while (jp.nextToken() != JsonToken.END_ARRAY) {
				if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
					List<Object> row = new ArrayList<Object>();
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						JsonToken t = jp.getCurrentToken();
						switch (t) {
						case VALUE_NUMBER_INT:
						case VALUE_NUMBER_FLOAT:
						case VALUE_STRING:
							row.add(jp.getText());
							break;

						case VALUE_TRUE:
							row.add(Boolean.TRUE);
							break;

						case VALUE_FALSE:
							row.add(Boolean.FALSE);
							break;

						case VALUE_NULL:
							row.add(null);
							break;

						case START_ARRAY:
							List<Object> array = new ArrayList<Object>();
							while (jp.nextToken() != JsonToken.END_ARRAY) {
								JsonToken t2 = jp.getCurrentToken();
								switch (t2) {
								case VALUE_NUMBER_INT:
								case VALUE_NUMBER_FLOAT:
								case VALUE_STRING:
									array.add(jp.getText());
									break;

								case VALUE_TRUE:
									array.add(Boolean.TRUE);
									break;

								case VALUE_FALSE:
									array.add(Boolean.FALSE);
									break;

								case VALUE_NULL:
									array.add(null);
									break;

								default:
									throw new IOException("Parse error");
								}
							}
							row.add(array);
							break;

						default:
							throw new IOException("Parse error");
						}
					}
					rowList.add(row.toArray());
				}
			}
		}
		Object[][] output = new Object[rowList.size()][];
		for (int i = 0; i < rowList.size(); i++) {
			output[i] = rowList.get(i);
		}
		return output;
	}
}
