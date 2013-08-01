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
package org.ambientdynamix.util;

/**
 * Used in the SeparatedListAdapter written by Jeff Sharkey (Copyright 2008 Jeff Sharkey and released under the Apache
 * 2.0 license). Interface that our adapters can implement to release any observers they may have registered with remote
 * resources manager. Most of the adapters register an observer in their constructor, but there is was no appropriate
 * place to release them. Parent activities can call this method in their onPause(isFinishing()) block to properly
 * release the observers. If the observers are not released, it will cause a memory leak.
 * 
 * @link 
 *       http://code.google.com/p/foursquared/source/browse/main/src/com/joelapenna/foursquared/widget/SeparatedListAdapter
 *       .java
 * @link http://www.jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
 * @date March 8, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public interface IObservableAdapter {
	public void removeObserver();
}
