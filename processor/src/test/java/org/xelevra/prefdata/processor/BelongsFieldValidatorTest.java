package org.xelevra.prefdata.processor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Stubber;
import org.xelevra.prefdata.annotations.Belongs;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BelongsFieldValidatorTest {
    @Mock private ProcessingEnvironment processingEnv;
    @Mock private Messager messager;

    private List<String> messages;
    private BelongsFieldValidator validator;

    @Before
    public void setup() {
        messages = new ArrayList<>();
        Stubber answer = doAnswer(invocation -> messages.add(invocation.getArgument(1).toString()));
        answer.when(messager).printMessage(any(), any(), any());

        when(processingEnv.getMessager()).thenReturn(messager);
        validator = new BelongsFieldValidator(processingEnv);
    }

    @Test
    public void checkFieldWithoutAnnotation() {
        createAndValidateField("batya", "int");
        assertMessages();
    }

    @Test
    public void checkCorrectIntField() {
        createAndValidateField("buratino", "int", "0", "-21", "324", "1234567890");
        assertMessages();
    }

    @Test
    public void checkErrorsOnInt() {
        TestField testField = createAndValidateField("chepalino", "int", allPossibleValues());
        assertMessages(
                testField.inconsistentError(1, "long"),
                testField.inconsistentError(3, "string"),
                testField.inconsistentError(4, "float"),
                testField.inconsistentError(5, "boolean"));
    }

    @Test
    public void checkCorrectLongField() {
        createAndValidateField("longTimeAgoFarFarAway", "long", "0", "-21", "55", "432453253221523412");
        assertMessages();
    }

    @Test
    public void checkErrorsOnLong() {
        TestField testField = createAndValidateField("longy", "long", allPossibleValues());
        assertMessages(
                testField.inconsistentError(3, "string"),
                testField.inconsistentError(4, "float"),
                testField.inconsistentError(5, "boolean"));
    }

    @Test
    public void checkCorrectFloatField() {
        createAndValidateField("floatInTheWater", "float", "0", "-21", "55", "43252352353444", "4.2", "0.0004f");
        assertMessages();
    }

    @Test
    public void checkErrorsOnFloat() {
        TestField testField = createAndValidateField("fallout", "float", allPossibleValues());
        assertMessages(
                testField.inconsistentError(3, "string"),
                testField.inconsistentError(5, "boolean"));
    }

    @Test
    public void checkCorrectBooleanField() {
        createAndValidateField("boboboo", "boolean", "true", "false");
        assertMessages();
    }

    @Test
    public void checkErrorsOnBoolean() {
        TestField testField = createAndValidateField("boooooool", "boolean", allPossibleValues());
        assertMessages(
                testField.inconsistentError(0, "int"),
                testField.inconsistentError(1, "long"),
                testField.inconsistentError(2, "int"),
                testField.inconsistentError(3, "string"),
                testField.inconsistentError(4, "float"));
    }

    @Test
    public void checkStringFieldAcceptsEverything() {
        createAndValidateField("greatEater", "java.lang.String", allPossibleValues());
        assertMessages();
    }

    private TestField createAndValidateField(String nameStr, String type, String... possibleValues) {
        TestField testField = new TestField(nameStr, type, possibleValues);
        validator.validateField(testField.getAsElement());
        return testField;
    }

    private void assertMessages(String... messages) {
        assertEquals(messages.length, this.messages.size());
        for (int i = 0; i < messages.length; i++) {
            assertEquals(messages[i], this.messages.get(i));
        }
    }

    private String[] allPossibleValues() {
        return new String[]{"2", "1000000000000", "5", "soso-picasso", "2.3", "false"};
    }

    private static final class TestField {
        private final String nameStr;
        private final String type;
        private final String[] possibleValues;

        public TestField(String nameStr, String type, String... possibleValues) {
            this.nameStr = nameStr;
            this.type = type;
            this.possibleValues = possibleValues;
        }

        public VariableElement getAsElement() {
            TypeMirror typeMirror = mock(TypeMirror.class);
            when(typeMirror.toString()).thenReturn(type);

            Belongs belongs = null;
            if (possibleValues.length != 0) {
                belongs = mock(Belongs.class);
                when(belongs.value()).thenReturn(possibleValues);
            }

            Name name = null;
            if (nameStr != null) {
                name = mock(Name.class);
                when(name.toString()).thenReturn(nameStr);
            }

            VariableElement element = mock(VariableElement.class);
            when(element.asType()).thenReturn(typeMirror);
            when(element.getAnnotation(Belongs.class)).thenReturn(belongs);
            when(element.getSimpleName()).thenReturn(name);
            return element;
        }

        public String inconsistentError(int possibleValueIndex, String actualType) {
            return nameStr + " is " + type + ", but one of possible values is " + possibleValues[possibleValueIndex] + " (" + actualType + ")";
        }
    }
}
