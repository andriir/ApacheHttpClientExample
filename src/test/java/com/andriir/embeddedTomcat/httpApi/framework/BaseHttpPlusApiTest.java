package au.com.clarity.test.httpApi.framework;

import au.com.clarity.auth.domain.globalaccount.UserType;
import au.com.clarity.auth.domain.security.AuthPermissionType;
import au.com.clarity.data.common.CompanyContext;
import au.com.clarity.data.domain.security.EffectivePermissionStatus;
import au.com.clarity.data.domain.security.PermissionAction;
import au.com.clarity.data.domain.security.PermissionGroup;
import au.com.clarity.data.service.login.FieldFilterCondition;
import au.com.clarity.data.util.UserAccountContext;
import au.com.clarity.integration.dto.company.CompanyAccessRight;
import au.com.clarity.integration.dto.company.CompanyAuth;
import au.com.clarity.integration.dto.globalaccount.GlobalAccount;
import au.com.clarity.integration.dto.login.UserAccount;
import au.com.clarity.integration.dto.security.PermissionGroupRow;
import au.com.clarity.integration.dto.security.RoleAssignStatusItem;
import au.com.clarity.integration.dto.security.RolePermissionModel;
import au.com.clarity.integration.dto.security.UserAccountRolesAssignStatusModel;
import au.com.clarity.integration.query.QueryFieldContainer;
import au.com.clarity.integration.service.company.CompanyAccessRightFilterItem;
import au.com.clarity.integration.service.company.CompanyAccessRightIntegrationService;
import au.com.clarity.integration.service.company.CompanyAuthIntegrationService;
import au.com.clarity.integration.service.company.DepartmentIntegrationService;
import au.com.clarity.integration.service.globalaccount.GlobalAccountIntegrationService;
import au.com.clarity.integration.service.location.LocationIntegrationService;
import au.com.clarity.integration.service.login.UserAccountIntegrationService;
import au.com.clarity.integration.service.login.UserRoleIntegrationService;
import au.com.clarity.test.api.framework.ManageBaseTest;
import au.com.clarity.test.httpApi.framework.data.HttpTestUser;
import au.com.clarity.test.httpApi.framework.harness.HttpClientHarness;
import au.com.clarity.test.httpApi.framework.harness.HttpRequestHarness;
import au.com.clarity.test.httpApi.framework.harness.HttpTestInfo;
import au.com.clarity.test.integration.builder.CompanyAccessRightTestBuilder;
import au.com.clarity.test.integration.builder.GlobalAccountTestBuilder;
import au.com.clarity.test.integration.builder.UserAccountTestBuilder;
import au.com.clarity.util.query.Command;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Andrew Redko on 12/10/15.
 */

@Test(singleThreaded = true)
public class BaseHttpPlusApiTest extends ManageBaseTest {

    @Autowired
    private UserRoleIntegrationService userRoleIntegrationService;

    @Autowired
    private LocationIntegrationService locationIntegrationService;

    @Autowired
    private DepartmentIntegrationService departmentIntegrationService;

    @Autowired
    private UserAccountIntegrationService userAccountIntegrationService;

    @Autowired
    private GlobalAccountIntegrationService globalAccountIntegrationService;

    @Autowired
    private CompanyAuthIntegrationService companyAuthIntegrationService;

    @Autowired
    private CompanyAccessRightIntegrationService companyAccessRightIntegrationService;

    @Value("${clarity.ui-test.company1.permissionTestUser.roleName}")
    protected String COMPANY1_USER_ROLE_NAME;

    @Value("${clarity.ui-test.company1.permissionTestUser.name}")
    protected String COMPANY1_USER_NAME;

    @Value("${clarity.ui-test.company1.permissionTestUser.email}")
    protected String COMPANY1_USER_EMAIL;

    @Value("${clarity.ui-test.company1.permissionTestUser.password}")
    protected String COMPANY1_USER_PASSWORD;

    @Value("${clarity.ui-test.locale}")
    private String UITEST_LOCALE;

    protected HttpClientHarness httpHarness;
    protected HttpRequestHarness formHarness;
    protected HttpTestInfo testInfo;

    @Test(priority = 10, enabled = true)
    @Transactional(value = "txnManagerAuth")
    @Rollback(false)
    public void suiteSetup() {
        testInfo = new HttpTestInfo();
        testInfo.setManageHostURL("localhost:8081");
        testInfo.setSiteHostURL("localhost:8099");
        testInfo.setManageAdminUser(new HttpTestUser("manage.admin@test.com", "123"));
        testInfo.setSiteUser(new HttpTestUser(COMPANY1_USER_EMAIL, COMPANY1_USER_PASSWORD));

        // make sure cookies is turn on
        CookieHandler.setDefault(new CookieManager());

        // setup Manage client
        HeaderGroup headerGroup = getManageHeaderGroup(testInfo);
        httpHarness = new HttpClientHarness(testInfo, headerGroup);
        formHarness = new HttpRequestHarness();

        //1.create user role
        createUserRoleTest();

        //2.create user
        createPermissionUserTest();

        //3.assign user with the role
        assignPermissionUserTest();
    }

    protected GlobalAccount globalAccount;
    protected RolePermissionModel roleModel;

//    @Test(priority = 100, enabled = true)
//    @Transactional(value = "txnManagerAuth")
//    @Rollback(false)
    private void createUserRoleTest() {
        //prepare RolePermission object
        roleModel = userRoleIntegrationService.getRolePermissionsForNewRole();
        roleModel.setCompanyId(companyDatabaseGUID);
        roleModel.getRole().setName(COMPANY1_USER_ROLE_NAME +testInfo.getTestGuid());

        //save role
        CompanyAuth companyAuth = companyAuthIntegrationService.findOne(companyDatabaseGUID, Arrays.asList(AuthPermissionType.IS_ORGANISATION_OWNER));

        CompanyContext.setCompanyGUID(companyAuth.getCompanyId().toString());
        UserAccountContext.setUserAccountId(1L);

        //update SALES_QUOTE_READ permission
        roleModel.getRows().get(PermissionGroup.SALES_QUOTE).getCells().get(PermissionAction.READ).setRolesPermissionEnabled(true);

        Long roleId = userRoleIntegrationService.saveRolePermissions(roleModel);
        roleModel.getRole().setPrimaryId(roleId);

        UserAccountContext.clear();
        CompanyContext.clear();
    }

//    @Test(priority = 110, enabled = true)
//    @Transactional(value = "txnManagerAuth")
//    @Rollback(false)
    private void createPermissionUserTest() {
        // ++++++++++++++++++++++++++++++++++++++++++
        // 1. Manage > Manage User Accounts > New User Account
        // Enter username, email, Active=Yes, and Standard_User
        globalAccount = applicationContext.getBean(GlobalAccountTestBuilder.class)
                .setName(COMPANY1_USER_NAME + testInfo.getTestGuid())
                .setEmailAddress(COMPANY1_USER_EMAIL.replaceAll("@", testInfo.getTestGuid() + "@"))
                .setPassword(COMPANY1_USER_PASSWORD)
                .setUserType(UserType.COMPANY_USER)
                .buildAndCreate();
    }

//    @Test(priority = 120, enabled = true)
//    @Transactional(value = "txnManagerAuth")
//    @Rollback(false)
    private void assignPermissionUserTest() {
        // +++++++++++++++++++++++++++++++++++++++++++
        // 3. Assign user to the company
        // 3.1 Choose user to assign (AssignUsersController.save)
        Long companyAccessRightId = globalAccount.getPrimaryId();
        List<QueryFieldContainer<CompanyAccessRightFilterItem>> queryFieldContainers = new ArrayList<QueryFieldContainer<CompanyAccessRightFilterItem>>();
        CompanyAuth loadedEntity = companyAuthIntegrationService.findOne(companyDatabaseGUID, 0, Integer.MAX_VALUE, queryFieldContainers);

        GlobalAccount gl1 = globalAccountIntegrationService.findByGlobalIdentifier(globalAccount.getGlobalIdentifier());
        CompanyAccessRight newCompanyAccessRight = applicationContext.getBean(CompanyAccessRightTestBuilder.class)
                .setCompany(loadedEntity)
                .setGlobalAccount(gl1)
                .buildAndCreate();

        loadedEntity.addAccessRight(newCompanyAccessRight);


        try {
            CompanyContext.setCompanyGUID(loadedEntity.getCompanyId().toString());
            UserAccountContext.setUserAccountId(1L);

            UserAccount userAccount = applicationContext.getBean(UserAccountTestBuilder.class)
                    .setGlobalIdentifier(globalAccount.getGlobalIdentifier())
                    .setEmailAddress(newCompanyAccessRight.getGlobalAccount().getEmailAddress())
                    .setName(newCompanyAccessRight.getGlobalAccount().getName())
                    .setLocaleString(UITEST_LOCALE)
                    .setLocation(locationIntegrationService.findAll().get(0))
                    .setDepartment(departmentIntegrationService.findAll().get(0))
                    .setManage(true)
                    .buildAndCreate();

            newCompanyAccessRight.setUserAccount(userAccount);
        } finally {
            UserAccountContext.setUserAccountId(null);
            CompanyContext.setCompanyGUID(null);
        }
        companyAuthIntegrationService.create(loadedEntity);// or update ?

        // addCompanyUserAccount
        for (CompanyAccessRight companyAccessRight : loadedEntity.getAccessRights()) {
            if (companyAccessRight.getCompany().getCompanyId() != null) {
                CompanyContext.setCompanyGUID(companyAccessRight.getCompany().getCompanyId().toString());
                companyAccessRight.setUserAccount(userAccountIntegrationService.findByGlobalIdentifier(companyAccessRight.getGlobalAccount().getGlobalIdentifier()));
                CompanyContext.setCompanyGUID(null);
            }
        }


        //prepare blank roleModel object, the same as Index method
        try {
            CompanyContext.setCompanyGUID(loadedEntity.getCompanyId().toString());
            UserAccountContext.setUserAccountId(1L);

            FieldFilterCondition<String> roleNameFilterCondition = new FieldFilterCondition<String>(Command.CONTAINS, "");
            UserAccountRolesAssignStatusModel userAccountRolesAssignStatusModel = userAccountIntegrationService.getUserAccountRolesAssignStatus(globalAccount.getPrimaryId(), roleNameFilterCondition);

            userAccountRolesAssignStatusModel.setMode("Edit");
            userAccountRolesAssignStatusModel.setCompanyAccessRightId(companyAccessRightId);

            //select just created role to assign for a user
            for (RoleAssignStatusItem roleItem : userAccountRolesAssignStatusModel.getRoleAssignStatusItems()) {
                if (roleItem.getUserRole() != null && roleItem.getUserRole().getName().equals(roleModel.getRole().getName())) {
                    roleItem.setAssigned(true);
                }
            }

            //save
            userAccountIntegrationService.saveUserAccountAssignedRoles(userAccountRolesAssignStatusModel);
        } finally {
            UserAccountContext.setUserAccountId(null);
            CompanyContext.setCompanyGUID(null);
        }

    }


    private final String USER_AGENT = "Mozilla/5.0";

    protected HeaderGroup getManageHeaderGroup(HttpTestInfo testInfo) {
        HeaderGroup headerGroup = new HeaderGroup();
        headerGroup.addHeader(new BasicHeader("Host", testInfo.getSiteHostURL()));
        headerGroup.addHeader(new BasicHeader("User-Agent", USER_AGENT));
        headerGroup.addHeader(new BasicHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
        headerGroup.addHeader(new BasicHeader("Accept-Language", "en-US,en;q=0.5"));
        headerGroup.addHeader(new BasicHeader("Connection", "keep-alive"));
        headerGroup.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        return headerGroup;
    }

    protected void updateRoleSetAllAllowedExcept(PermissionGroup permGroup, PermissionAction permAction) {
        //1.1. set all permissions to ALLOWED
        for (PermissionGroupRow row : roleModel.getRows().values()) {
            for (EffectivePermissionStatus status : row.getCells().values()) {
                if (status != null) {
                    status.setRolesPermissionEnabled(true);
                }
            }
        }

        //1.2. set provided permission to DENIED
        roleModel.getRows().get(permGroup).getCells().get(permAction).setRolesPermissionEnabled(false);

        //1.3. update the user role
        CompanyAuth companyAuth = companyAuthIntegrationService.findOne(companyDatabaseGUID, Arrays.asList(AuthPermissionType.IS_ORGANISATION_OWNER));
        CompanyContext.setCompanyGUID(companyAuth.getCompanyId().toString());
        UserAccountContext.setUserAccountId(1L);

        userRoleIntegrationService.saveRolePermissions(roleModel);

        UserAccountContext.clear();
        CompanyContext.clear();
    }




}
