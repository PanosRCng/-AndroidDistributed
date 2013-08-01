/*
 * Copyright (C) The Ambient Dynamix Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambientdynamix.core;

import java.util.List;

import org.ambientdynamix.security.PrivacyPolicy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * User interface listing installed ContextPlugin privacy policies.
 * 
 * @see PrivacyPolicy
 * @author Darren Carlson
 */
public class PrivacyActivity extends Activity {
	// Private data
	private ListView policyList = null;
	private PrivacyPolicyAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.privacy_tab);
		policyList = (ListView) findViewById(R.id.privacy_policy_list);
		policyList.setClickable(true);
		policyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				PrivacyPolicy policy = (PrivacyPolicy) policyList.getItemAtPosition(position);
				Bundle bundle = new Bundle();
				bundle.putSerializable("policy", policy);
				Intent i = new Intent(PrivacyActivity.this, PrivacyDetailsActivity.class);
				i.putExtras(bundle);
				startActivity(i);
			}
		});
		// Setup the button
		Button btnAddNewPrivacyPolicy = (Button) findViewById(R.id.btn_add_new_privacy_policy);
		btnAddNewPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// Do something thrilling here!
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.adapter = new PrivacyPolicyAdapter(this, R.layout.icon_row, DynamixService.getPrivacyPolicies());
		policyList.setAdapter(adapter);
	};

	/**
	 * Local class used as a datasource for PrivacyPolicy entities. This class extends a typed Generic ArrayAdapter and
	 * overrides getView in order to update the UI state.
	 * 
	 * @author Darren Carlson
	 */
	private class PrivacyPolicyAdapter extends ArrayAdapter<PrivacyPolicy> {
		private List<PrivacyPolicy> policies;

		public PrivacyPolicyAdapter(Context context, int textViewResourceId, List<PrivacyPolicy> policies) {
			super(context, textViewResourceId, policies);
			this.policies = policies;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.icon_row, null);
			}
			PrivacyPolicy policy = policies.get(position);
			if (policy != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				ImageView icon = (ImageView) v.findViewById(R.id.icon);
				icon.setImageResource(R.drawable.profile);
				if (tt != null) {
					tt.setText(policy.getName());
				}
				if (bt != null) {
					bt.setText(R.string.privacy_policy_click_me);
				}
			}
			return v;
		}
	}
}