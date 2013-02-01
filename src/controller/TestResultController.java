package controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import model.CustomDateFormatter;
import model.admin.ConfigPropertyConstants;
import model.bloodtest.BloodTest;
import model.collectedsample.CollectedSample;
import model.testresults.TestResult;
import model.testresults.TestResultBackingForm;
import model.testresults.TestResultBackingFormValidator;
import model.worksheet.CollectionsWorksheet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import repository.BloodTestRepository;
import repository.CollectedSampleRepository;
import repository.GenericConfigRepository;
import repository.TestResultRepository;
import viewmodel.CollectedSampleViewModel;
import viewmodel.TestResultViewModel;

@Controller
public class TestResultController {
  @Autowired
  private CollectedSampleRepository collectedSampleRepository;

  @Autowired
  private TestResultRepository testResultRepository;

  @Autowired
  private BloodTestRepository bloodTestRepository;

  @Autowired
  private GenericConfigRepository genericConfigRepository; 

  @Autowired
  private UtilController utilController;

  public TestResultController() {
  }

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(new TestResultBackingFormValidator(binder.getValidator(), utilController));
  }

  @RequestMapping(value = "/findTestResultFormGenerator", method = RequestMethod.GET)
  public ModelAndView findTestResultFormGenerator(HttpServletRequest request, Model model) {

    TestResultBackingForm form = new TestResultBackingForm();
    model.addAttribute("findTestResultForm", form);

    ModelAndView mv = new ModelAndView("findTestResultForm");
    Map<String, Object> m = model.asMap();
    utilController.addTipsToModel(model.asMap(), "testResults.find");
    addEditSelectorOptions(m);
    // to ensure custom field names are displayed in the form
    m.put("testResultFields", utilController.getFormFieldsForForm("testResult"));
    m.put("refreshUrl", getUrl(request));
    mv.addObject("model", m);
    return mv;
  }

  @RequestMapping(value = "/testResultSummary", method = RequestMethod.GET)
  public ModelAndView testResultSummaryGenerator(HttpServletRequest request, Model model,
      @RequestParam(value = "testResultId", required = false) Long testResultId) {

    ModelAndView mv = new ModelAndView("testResultSummary");
    Map<String, Object> m = model.asMap();

    m.put("requestUrl", getUrl(request));

    TestResult testResult = null;
    if (testResultId != null) {
      testResult = testResultRepository.findTestResultById(testResultId);
      if (testResult != null) {
        m.put("existingTestResult", true);
      }
      else {
        m.put("existingTestResult", false);
      }
    }

    TestResultViewModel testResultViewModel = getTestResultViewModels(Arrays.asList(testResult)).get(0);
    m.put("testResult", testResultViewModel);
    m.put("refreshUrl", getUrl(request));
    // to ensure custom field names are displayed in the form
    m.put("testResultFields", utilController.getFormFieldsForForm("TestResult"));
    mv.addObject("model", m);
    return mv;
  }

  public static String getUrl(HttpServletRequest req) {
    String reqUrl = req.getRequestURL().toString();
    String queryString = req.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }

  private void addEditSelectorOptions(Map<String, Object> m) {
    m.put("bloodTests", bloodTestRepository.getAllBloodTests());
  }

  @RequestMapping(value = "/editTestResultFormGenerator", method = RequestMethod.GET, 
      headers = "X-Requested-With=XMLHttpRequest")
  public ModelAndView editTestResultFormGenerator(HttpServletRequest request,
      Model model,
      //@RequestParam(value="collectionNumber", required=false) Long collectionNumber,
      @RequestParam(value="testResultId", required=false) Long testResultId) {

    TestResultBackingForm form = new TestResultBackingForm();

    System.out.println("");
    ModelAndView mv = new ModelAndView("editTestResultForm");
    Map<String, Object> m = model.asMap();
    m.put("refreshUrl", getUrl(request));
    m.put("existingTestResult", false);
    if (testResultId != null) {
      form.setId(testResultId);
      TestResult testResult = testResultRepository.findTestResultById(testResultId);
      if (testResult != null) {
        form = new TestResultBackingForm(testResult);
        m.put("existingTestResult", true);
      }
    }

    addEditSelectorOptions(m);
    m.put("editTestResultForm", form);
    m.put("refreshUrl", getUrl(request));
    // to ensure custom field names are displayed in the form
    m.put("testResultFields", utilController.getFormFieldsForForm("TestResult"));
    System.out.println(m);
    mv.addObject("model", m);
    System.out.println(mv);
    System.out.println(mv.getView());
    return mv;
  }

  @RequestMapping(value = "/addAllTestResultsFormGenerator", method = RequestMethod.GET, 
      headers = "X-Requested-With=XMLHttpRequest")
  public ModelAndView addAllTestResultsFormGenerator(HttpServletRequest request,
      Model model) {

    System.out.println("");
    ModelAndView mv = new ModelAndView("addAllTestResultsForm");
    Map<String, Object> m = model.asMap();
    m.put("refreshUrl", getUrl(request));
    addEditSelectorOptions(m);
    // to ensure custom field names are displayed in the form
    m.put("testResultFields", utilController.getFormFieldsForForm("TestResult"));
    mv.addObject("model", m);
    return mv;
  }

  @RequestMapping(value = "/addTestResult", method = RequestMethod.POST)
  public ModelAndView addTestResult(
      HttpServletRequest request,
      HttpServletResponse response,
      @ModelAttribute("editTestResultForm") @Valid TestResultBackingForm form,
      BindingResult result, Model model) {

    ModelAndView mv = new ModelAndView("editTestResultForm");
    boolean success = false;
    String message = "";
    Map<String, Object> m = model.asMap();

    // IMPORTANT: Validation code just checks if the ID exists.
    // We still need to store the collected sample as part of the product.
    String collectionNumber = form.getCollectionNumber();
    if (collectionNumber != null && !collectionNumber.isEmpty()) {
      try {
        CollectedSample collectedSample = collectedSampleRepository.findSingleCollectedSampleByCollectionNumber(collectionNumber);
        form.setCollectedSample(collectedSample);
      } catch (NoResultException ex) {
        form.setCollectedSample(null);
        ex.printStackTrace();
      }
    } else {
      form.setCollectedSample(null);
    }

    if (result.hasErrors()) {
      m.put("hasErrors", true);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);      
      success = false;
      message = "Please fix the errors noted above.";
      System.out.println("there are errors");
      for (ObjectError error : result.getAllErrors()) {
        System.out.println(error.getObjectName());
        System.out.println(error.getCode());
        System.out.println(form.getTestResult());
        System.out.println(error.toString());
        System.out.println(error.getDefaultMessage());
      }
    } else {
      try {
        TestResult testResult = form.getTestResult();
        testResult.setIsDeleted(false);
        testResultRepository.addTestResult(testResult);
        m.put("hasErrors", false);
        success = true;
        message = "Test Result Successfully Added";
        form = new TestResultBackingForm();
      } catch (EntityExistsException ex) {
        ex.printStackTrace();
        success = false;
        message = "Test Result Already exists.";
      } catch (Exception ex) {
        ex.printStackTrace();
        success = false;
        message = "Internal Error. Please try again or report a Problem.";
      }
    }

    m.put("editTestResultForm", form);
    m.put("existingTestResult", false);
    m.put("success", success);
    m.put("message", message);
    m.put("refreshUrl", "editTestResultFormGenerator.html");
    m.put("testResultFields", utilController.getFormFieldsForForm("TestResult"));
    addEditSelectorOptions(m);

    mv.addObject("model", m);
    return mv;
  }

  @RequestMapping(value = "/addAllTestResults", method = RequestMethod.POST)
  public ModelAndView addAllTestResults(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam Map<String, String> params) {

    ModelAndView mv = new ModelAndView("addAllTestResultsForm");
    Boolean hasErrors = false; 

    Map<String, Object> m = new HashMap<String, Object>();

    System.out.println(params);
    // IMPORTANT: Validation code just checks if the ID exists.
    // We still need to store the collected sample as part of the product.
    String collectionNumber = params.get("collectionNumber");
    CollectedSample collectedSample = null;
    if (collectionNumber == null) {
      hasErrors = true;
      m.put("collectionNumberError", "Collection does not exist");
    }
    else {
      try {
        collectedSample = collectedSampleRepository.findSingleCollectedSampleByCollectionNumber(collectionNumber);
        if (collectedSample == null) {
          hasErrors = true;
          m.put("collectionNumberError", "Collection does not exist");
        }
      } catch (NoResultException ex) {
        ex.printStackTrace();
        hasErrors = true;
        m.put("collectionNumberError", "Collection does not exist");
      }
    }

    String testedOnStr = params.get("testedOn");
    Date testedOn = null;
    try {
        testedOn = CustomDateFormatter.getDateFromString(testedOnStr);
    } catch (ParseException e) {
      hasErrors = true;
      m.put("testedOnError", CustomDateFormatter.getErrorMessage());
      e.printStackTrace();
    }

    m.put("refreshUrl", getUrl(request));
    addEditSelectorOptions(m);
    // to ensure custom field names are displayed in the form
    m.put("testResultFields", utilController.getFormFieldsForForm("TestResult"));
    mv.addObject("model", m);

    if (hasErrors) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return mv;
    }

    String notes = params.get("notes");

    for (Entry<String, String> param : params.entrySet()) {
      String name = param.getKey();
      if (!name.startsWith("Test"))
        continue;

      String testName = name.substring(4);
      String testResult = param.getValue();

      BloodTest bloodTest = bloodTestRepository.findBloodTestByName(testName);
      TestResult t = new TestResult();
      t.setCollectedSample(collectedSample);
      t.setTestedOn(testedOn);
      t.setBloodTest(bloodTest);
      t.setResult(testResult);
      t.setIsDeleted(false);
      t.setNotes(notes);
      try {
        testResultRepository.addTestResult(t);
      } catch (Exception ex) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    }

    return mv;
  }

  @RequestMapping("/findTestResult")
  public ModelAndView findTestResult(HttpServletRequest request,
      @ModelAttribute("findTestResultForm") TestResultBackingForm form,
      BindingResult result, Model model) {

    List<TestResult> testResults = Arrays.asList(new TestResult[0]);

    String dateTestedFrom = form.getDateTestedFrom();
    String dateTestedTo = form.getDateTestedTo();
    testResults = testResultRepository.findTestResults(
                                        form.getCollectionNumber(),
                                        dateTestedFrom, dateTestedTo);
    System.out.println(testResults);

    ModelAndView modelAndView = new ModelAndView("testResultsTable");
    Map<String, Object> m = model.asMap();
    m.put("tableName", "findTestResultsTable");
    m.put("testResultFields", utilController.getFormFieldsForForm("TestResult"));
    m.put("allTestResults", getTestResultViewModels(testResults));
    m.put("refreshUrl", getUrl(request));
    addEditSelectorOptions(m);

    modelAndView.addObject("model", m);
    return modelAndView;
  }

  @RequestMapping(value = "/updateTestResult", method = RequestMethod.POST)
  public ModelAndView updateTestResult(
      HttpServletResponse response,
      @ModelAttribute("editTestResultForm") @Valid TestResultBackingForm form,
      BindingResult result, Model model) {

    ModelAndView mv = new ModelAndView("editTestResultForm");
    boolean success = false;
    String message = "";
    Map<String, Object> m = model.asMap();
    addEditSelectorOptions(m);
    // only when the collection is correctly added the existingCollectedSample
    // property will be changed
    m.put("existingTestResult", true);

    if (result.hasErrors()) {
      m.put("hasErrors", true);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      success = false;
      message = "Please fix the errors noted above now!";
    }
    else {
      try {

        form.setIsDeleted(false);
        TestResult existingTestResult = testResultRepository.updateTestResult(form.getTestResult());
        if (existingTestResult == null) {
          m.put("hasErrors", true);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          success = false;
          m.put("existingTestResult", false);
          message = "Test Result does not already exist.";
        }
        else {
          m.put("hasErrors", false);
          success = true;
          message = "Test Result Successfully Updated";
        }
      } catch (EntityExistsException ex) {
        ex.printStackTrace();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        success = false;
        message = "Test Result Already exists.";
      } catch (Exception ex) {
        ex.printStackTrace();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        success = false;
        message = "Internal Error. Please try again or report a Problem.";
      }
   }

    m.put("editTestResultForm", form);
    m.put("success", success);
    m.put("message", message);
    m.put("testResultFields", utilController.getFormFieldsForForm("TestResult"));

    mv.addObject("model", m);

    return mv;
  }

  @RequestMapping(value = "/deleteTestResult", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, ? extends Object> deleteTestResult(
      @RequestParam("testResultId") Long testResultId) {

    boolean success = true;
    String errMsg = "";
    try {
      testResultRepository.deleteTestResult(testResultId);
    } catch (Exception ex) {
      // TODO: Replace with logger
      System.err.println("Internal Exception");
      System.err.println(ex.getMessage());
      success = false;
      errMsg = "Internal Server Error";
    }

    Map<String, Object> m = new HashMap<String, Object>();
    m.put("success", success);
    m.put("errMsg", errMsg);
    return m;
  }

  @RequestMapping(value="/ttiWorksheet", method=RequestMethod.GET)
  public ModelAndView getTtiWorkSheetFormGenerator(HttpServletRequest request,
      HttpServletResponse response) {
    ModelAndView mv = new ModelAndView("ttiWorksheetForm");
    return mv;
  }

  @RequestMapping(value="/showTTIWorksheet", method=RequestMethod.GET)
  public ModelAndView getTtiWorkSheetFormGenerator(HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(value="dateCollectedFrom", required=false) String dateCollectedFrom,
      @RequestParam(value="dateCollectedTo", required=false) String dateCollectedTo) {
    ModelAndView mv = new ModelAndView("ttiWorksheet");
    List<CollectedSample> untestedCollections = testResultRepository.findUntestedCollections(dateCollectedFrom, dateCollectedTo);
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("allUntestedCollectedSamples", untestedCollections);
    m.put("testResultFields", utilController.getFormFieldsForForm("testResults"));
    mv.addObject("model", m);
    return mv;
  }

  public static List<TestResultViewModel> getTestResultViewModels(
      List<TestResult> testResults) {
    if (testResults == null)
      return Arrays.asList(new TestResultViewModel[0]);
    List<TestResultViewModel> testResultViewModels = new ArrayList<TestResultViewModel>();
    for (TestResult testResult : testResults) {
      testResultViewModels.add(new TestResultViewModel(testResult));
    }
    return testResultViewModels;
  }

  @RequestMapping(value = "/worksheetForTestResultsFormGenerator", method = RequestMethod.GET)
  public ModelAndView findWorksheetForTestResultsFormGenerator(HttpServletRequest request, Model model) {

    ModelAndView mv = new ModelAndView("findWorksheetForTestResults");
    Map<String, Object> m = model.asMap();
    m.put("bloodTests", bloodTestRepository.getAllBloodTests());
    m.put("testResultFields", utilController.getFormFieldsForForm("testResult"));
    m.put("refreshUrl", getUrl(request));
    List<String> propertyOwners = Arrays.asList(ConfigPropertyConstants.COLLECTIONS_WORKSHEET);
    m.put("worksheetConfig", genericConfigRepository.getConfigProperties(propertyOwners));
    mv.addObject("model", m);
    return mv;
  }

  @RequestMapping(value="/editTestResultsForWorksheet", method=RequestMethod.GET)
  public ModelAndView editTestResultsForWorksheet(HttpServletRequest request, Model model,
      @RequestParam(value="worksheetBatchId") String worksheetBatchId) {

    ModelAndView mv = new ModelAndView("worksheetForTestResults");
    Map<String, Object> m = model.asMap();
    m.put("worksheetFound", true);
    m.put("bloodTests", bloodTestRepository.getAllBloodTests());
    List<String> propertyOwners = Arrays.asList(ConfigPropertyConstants.COLLECTIONS_WORKSHEET);
    m.put("worksheetConfig", genericConfigRepository.getConfigProperties(propertyOwners));
    m.put("worksheetBatchId", worksheetBatchId);
    m.put("nextPageUrl", getNextPageUrl(request));
    mv.addObject("model", m);

    return mv;
  }

  @RequestMapping(value="/editTestResultsForWorksheetPagination", method=RequestMethod.GET)
  public @ResponseBody Map<String, Object> editTestResultsForWorksheetPagination(HttpServletRequest request, Model model,
      @RequestParam(value="worksheetBatchId") String worksheetBatchId) {

    Map<String, Object> pagingParams = utilController.parsePagingParameters(request);
    List<Object> results = collectedSampleRepository.findCollectionsInWorksheet(worksheetBatchId, pagingParams);

    List<CollectedSample> collectedSamples = (List<CollectedSample>) results.get(0);
    CollectionsWorksheet worksheet = (CollectionsWorksheet) results.get(1);
    Long totalRecords = (Long) results.get(2);
    return generateDatatablesMap(worksheet, collectedSamples, totalRecords);
  }

  /**
   * Datatables on the client side expects a json response for rendering data from the server
   * in jquery datatables. Remember of columns is important and should match the column headings
   * in collectionsTable.jsp.
   * @param worksheet 
   */
  private Map<String, Object> generateDatatablesMap(
      CollectionsWorksheet worksheet,
      List<CollectedSample> collectedSamples, Long totalRecords) {

    List<String> propertyOwners = Arrays.asList(ConfigPropertyConstants.COLLECTIONS_WORKSHEET);
    Map<String, String> properties = genericConfigRepository.getConfigProperties(propertyOwners);
    List<BloodTest> bloodTests = bloodTestRepository.getAllBloodTests();

    Map<String, Object> resultsMap = new HashMap<String, Object>();
    ArrayList<Object> resultList = new ArrayList<Object>();

    Map<Long, List<TestResult>> testResultsByCollection = new HashMap<Long, List<TestResult>>();
    for (TestResult t : worksheet.getTestResults()) {
      if (t.getIsDeleted())
        continue;
      Long collectionId = t.getCollectedSample().getId();
      if (!testResultsByCollection.containsKey(collectionId)) {
        testResultsByCollection.put(collectionId, new ArrayList<TestResult>());
      }
      List<TestResult> testResults = testResultsByCollection.get(collectionId);
      testResults.add(t);
    }

    for (CollectedSampleViewModel collection : CollectedSampleController.getCollectionViewModels(collectedSamples)) {

      List<Object> row = new ArrayList<Object>();
      row.add(collection.getId());

      if (properties.containsKey("collectionNumber") && properties.get("collectionNumber").equals("true")) {
          row.add(collection.getCollectionNumber());
      }

      Map<String, TestResult> testResults = new HashMap<String, TestResult>();

      Date testedOn = null;
      if (testResultsByCollection.containsKey(collection.getId())) {
        for (TestResult t : testResultsByCollection.get(collection.getId())) {
          testedOn = t.getTestedOn();
          testResults.put(t.getBloodTest().getName(), t);
        }
      }

      if (properties.containsKey("testedOn") && properties.get("testedOn").equals("true")) {
          row.add(CustomDateFormatter.getDateString(testedOn));
      }

      // now add results for existing tests related to this worksheet
      for (BloodTest bt : bloodTests) {
        String testName = bt.getName();
        if (properties.containsKey(testName) && properties.get(testName).equals("true")) {
            if (testResults.containsKey(testName))
              row.add(testResults.get(testName).getResult());
            else
              row.add("");
        }
      }

      resultList.add(row);
    }
    resultsMap.put("aaData", resultList);
    resultsMap.put("iTotalRecords", totalRecords);
    resultsMap.put("iTotalDisplayRecords", totalRecords);
    return resultsMap;
  }

  private String getNextPageUrl(HttpServletRequest request) {
    String reqUrl = request.getRequestURL().toString().replaceFirst("editTestResultsForWorksheet.html", "editTestResultsForWorksheetPagination.html");
    String queryString = request.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }
}
