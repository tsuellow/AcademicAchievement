package com.example.android.aprocessor


import com.example.android.annotations.AsState
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.classFor
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.jvm.KotlinClassMetadata
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(AsState::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(AsState::class.java)
            .forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only classes can be annotated")
                    return true
                }
                processAnnotation(it)
            }
        return false
    }

    @OptIn(KotlinPoetMetadataPreview::class, com.squareup.kotlinpoet.DelicateKotlinPoetApi::class)
    private fun processAnnotation(element: Element) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()
        val kmClass = (element as TypeElement).toImmutableKmClass()

        //create vessel for mutable state class
        val mutableFileName = "${className}MutableState"
        val mutableFileBuilder= FileSpec.builder(pack, mutableFileName)
        val mutableClassBuilder = TypeSpec.classBuilder(mutableFileName)
        val mutableConstructorBuilder= FunSpec.constructorBuilder()
            .addParameter("rootObject",element.asType().asTypeName())
        var helper="return ${element.simpleName}("

        //create vessel for immutable state class
        val stateFileName = "${className}State"
        val stateFileBuilder= FileSpec.builder(pack, stateFileName)
        val stateClassBuilder = TypeSpec.classBuilder(stateFileName)
        val stateConstructorBuilder= FunSpec.constructorBuilder()
            .addParameter("mutableObject",ClassName(pack,mutableFileName))

        //import state related libraries
        val mutableStateClass= ClassName("androidx.compose.runtime","MutableState")
        val stateClass=ClassName("androidx.compose.runtime","State")
        val snapshotStateMap= ClassName("androidx.compose.runtime.snapshots","SnapshotStateMap")
        val snapshotStateList=ClassName("androidx.compose.runtime.snapshots","SnapshotStateList")


        fun processMapParameter(property: ImmutableKmValueParameter) {
            val clName =
                ((property.type?.abbreviatedType?.classifier) as KmClassifier.TypeAlias).name
            val arguments = property.type?.abbreviatedType?.arguments?.map {
                ClassInspectorUtil.createClassName(
                    ((it.type?.classifier) as KmClassifier.Class).name
                )
            }
            val paramClass = ClassInspectorUtil.createClassName(clName)
            val elementPackage = clName.replace("/", ".")
            val paramName = property.name

            arguments?.let {
                mutableClassBuilder.addProperty(
                    PropertySpec.builder(
                        paramName,
                        snapshotStateMap.parameterizedBy(it), KModifier.PUBLIC
                    )
                        .build()
                )
            }
            arguments?.let {
                stateClassBuilder.addProperty(
                    PropertySpec.builder(
                        paramName,
                        snapshotStateMap.parameterizedBy(it), KModifier.PUBLIC
                    )
                        .build()
                )
            }

            helper = helper.plus("${paramName} = ${paramClass.simpleName}(this.${paramName}),\n")

            mutableConstructorBuilder
                .addStatement("this.${paramName}=rootObject.${paramName}.map{Pair(it.key,it.value)}.toMutableStateMap()")

            stateConstructorBuilder
                .addStatement("this.${paramName}=mutableObject.${paramName}")
        }

        fun processListParameter(property: ImmutableKmValueParameter) {
            val clName =
                ((property.type?.abbreviatedType?.classifier) as KmClassifier.TypeAlias).name
            val arguments = property.type?.abbreviatedType?.arguments?.map {
                ClassInspectorUtil.createClassName(
                    ((it.type?.classifier) as KmClassifier.Class).name
                )
            }
            val paramClass = ClassInspectorUtil.createClassName(clName)
            val elementPackage = clName.replace("/", ".")
            val paramName = property.name

            arguments?.let {
                mutableClassBuilder.addProperty(
                    PropertySpec.builder(
                        paramName,
                        snapshotStateList.parameterizedBy(it), KModifier.PUBLIC
                    )
                        .build()
                )
            }
            arguments?.let {
                stateClassBuilder.addProperty(
                    PropertySpec.builder(
                        paramName,
                        snapshotStateList.parameterizedBy(it), KModifier.PUBLIC
                    )
                        .build()
                )
            }

            helper = helper.plus("${paramName} = ${paramClass.simpleName}(this.${paramName}),\n")

            mutableConstructorBuilder
                .addStatement("this.${paramName}=rootObject.${paramName}.toMutableStateList()")

            stateConstructorBuilder
                .addStatement("this.${paramName}=mutableObject.${paramName}")
        }

        fun processDefaultParameter(property: ImmutableKmValueParameter) {
            val clName = ((property.type?.classifier) as KmClassifier.Class).name
            val paramClass = ClassInspectorUtil.createClassName(clName)
            val elementPackage = clName.replace("/", ".")
            val paramName = property.name

            mutableClassBuilder.addProperty(
                PropertySpec.builder(
                    paramName,
                    mutableStateClass.parameterizedBy(paramClass), KModifier.PUBLIC
                ).build()
            )
            stateClassBuilder.addProperty(
                PropertySpec.builder(
                    paramName,
                    stateClass.parameterizedBy(paramClass),
                    KModifier.PUBLIC
                ).build()
            )

            helper = helper.plus("${paramName} = this.${paramName}.value,\n")

            mutableConstructorBuilder
                .addStatement(
                    "this.${paramName}=mutableStateOf(rootObject.${paramName}) "
                )

            stateConstructorBuilder
                .addStatement("this.${paramName}=mutableObject.${paramName}")
        }

        for (property in kmClass.constructors[0].valueParameters) {
            val javaPackage = (property.type!!.classifier as KmClassifier.Class).name.replace("/", ".")
            val javaClass=try {
                Class.forName(javaPackage)
            }catch (e:Exception){
                String::class.java
            }

            when{
                Map::class.java.isAssignableFrom(javaClass) ->{ //if property is of type map
                    processMapParameter(property)
                }
                List::class.java.isAssignableFrom(javaClass) ->{ //if property is of type list
                    processListParameter(property)
                }
                else ->{ //all others
                    processDefaultParameter(property)
                }
            }
        }

        helper=helper.plus(")") //close off method

        val getRootBuilder= FunSpec.builder("get$className")
            .returns(element.asClassName())
        getRootBuilder.addStatement(helper.toString())
        mutableClassBuilder.addFunction(mutableConstructorBuilder.build()).addFunction(getRootBuilder.build())
        stateClassBuilder.addFunction(stateConstructorBuilder.build())

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]

        val mutableFile = mutableFileBuilder
            .addImport("androidx.compose.runtime", "mutableStateOf")
            .addImport("androidx.compose.runtime","toMutableStateMap")
            .addImport("androidx.compose.runtime","toMutableStateList")
            .addType(mutableClassBuilder.build())
            .build()
        mutableFile.writeTo(File(kaptKotlinGeneratedDir))

        val stateFile = stateFileBuilder
            .addType(stateClassBuilder.build())
            .build()
        stateFile.writeTo(File(kaptKotlinGeneratedDir))
    }
}


//        for (property in element.enclosedElements) {
//
//            if (property.kind == ElementKind.FIELD) {
//                //val typeMetadata = property.
//                //val kmClass = (property as TypeElement).toImmutableKmClass()
//                val propName = ClassInspectorUtil.createClassName(kmClass.name)
//
//                mutableClassBuilder.addProperty(
//                    PropertySpec.builder(property.simpleName.toString(),
//                        mutableStateClass.parameterizedBy(propName.copy(nullable = false)),
//                        KModifier.PUBLIC)
//                        .build()
//                )
//                stateClassBuilder.addProperty(
//                    PropertySpec.builder(property.simpleName.toString(),
//                        stateClass.parameterizedBy(propName.copy(nullable = false)),
//                        KModifier.PUBLIC)
//                        .build()
//                )
//                val testi=JSONArray()
//                with(property){
//
//                }
//                mutableConstructorBuilder
//                    .addStatement("this.${property.simpleName.toString()}=mutableStateOf(rootObject.${property.simpleName.toString()}) " +
//                            "${JSONObject(String::class.toImmutableKmClass().supertypes)} "
//                            //JSONObject("{name: ${property.asType().javaClass.canonicalName}") +
//                            //JSONObject(property.asType().javaClass.annotatedInterfaces).toString()
//                    )
//
//                stateConstructorBuilder
//                    .addStatement("this.${property.simpleName.toString()}=mutableObject.${property.simpleName.toString()}")
//            }
//        }