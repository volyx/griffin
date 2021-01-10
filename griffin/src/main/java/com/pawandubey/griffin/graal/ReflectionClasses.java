package com.pawandubey.griffin.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jni.JNIRuntimeAccess;
import com.pawandubey.griffin.SingleIndex;
import com.pawandubey.griffin.model.Page;
import com.pawandubey.griffin.model.Post;
import com.threecrickets.jygments.contrib.Css2Lexer;
import com.threecrickets.jygments.grammar.DelegatedLexer;
import com.threecrickets.jygments.grammar.RegexLexer;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Arrays;

@AutomaticFeature
class ReflectionClasses implements Feature {

	@Override
	public void duringSetup(DuringSetupAccess access) {
	}

	@Override
	public void beforeAnalysis(BeforeAnalysisAccess access) {
		try {
//			JNIRuntimeAccess.register(NativeDB.class.getDeclaredMethod("_open_utf8", byte[].class, int.class));
		} catch (Exception e){
			e.printStackTrace();
		}
		setupClasses();
	}

	/**
	 * All classes defined here will have reflection support and be registered as spring beans
	 */
	static Class<?>[] getBeans(){
		return new Class[]{
		};
	}

	/**
	 * All classes defined here will have reflection support
	 */
	static Class<?>[] getClasses(){
		return new Class[]{
				RegexLexer.class,
				DelegatedLexer.class,
				Css2Lexer.class,
				com.threecrickets.jygments.contrib.Default2Style.class,
				com.threecrickets.jygments.contrib.BasicStyle.class,

				com.fasterxml.jackson.databind.ext.Java7SupportImpl.class,

				Post.class,
				SingleIndex.class,
				Page.class,

				java.util.HashMap.class,
				java.util.ArrayList.class
		};
	}

	static void setupClasses() {
		try {
			System.out.println("> Loading classes for future reflection support");
			for (final Class<?> clazz : getBeans()) {
				process(clazz);
			}
			for (final Class<?> clazz : getClasses()) {
				process(clazz);
			}
		} catch (Error e){
			if(!e.getMessage().contains("The class ImageSingletons can only be used when building native images")){
				throw e;
			}
		}
	}

	/**
	 * Register all constructors and methods on graalvm to reflection support at runtime
	 */
	private static void process(Class<?> clazz) {
		try {
			System.out.println("> Declaring class: " + clazz.getCanonicalName());
			RuntimeReflection.register(clazz);
			for (final Method method : clazz.getDeclaredMethods()) {
				System.out.println("\t> method: " + method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")");
				JNIRuntimeAccess.register(method);
				RuntimeReflection.register(method);
			}
			for (final Field field : clazz.getDeclaredFields()) {
				System.out.println("\t> field: " + field.getName());
				JNIRuntimeAccess.register(field);
				RuntimeReflection.register(field);
			}
			for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
				System.out.println("\t> constructor: " + constructor.getName() + "(" + constructor.getParameterCount() + ")");
				JNIRuntimeAccess.register(constructor);
				RuntimeReflection.register(constructor);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
