/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2008 Sun Microsystems, Inc.
 * Portions Copyright 2014-2016 ForgeRock AS.
 */
package org.opends.server.core;

import java.util.ArrayList;
import java.util.List;

import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.DN;
import org.forgerock.opendj.ldap.ResultCode;
import org.opends.server.TestCaseUtils;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.tools.LDAPAuthenticationHandler;
import org.opends.server.tools.LDAPCompare;
import org.opends.server.tools.LDAPDelete;
import org.opends.server.tools.LDAPModify;
import org.opends.server.tools.LDAPSearch;
import org.opends.server.tools.RemoteConnection;
import org.opends.server.types.AuthenticationInfo;
import org.opends.server.types.Control;
import org.opends.server.types.LDAPException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.opends.server.TestCaseUtils.*;
import static org.opends.server.protocols.internal.InternalClientConnection.*;
import static org.opends.server.util.ServerConstants.*;
import static org.testng.Assert.*;

/**
 * A set of testcases for configuration attribute
 * "ds-cfg-reject-unauthenticated-requests".
 */
@SuppressWarnings("javadoc")
public class RejectUnauthReqTests extends CoreTestCase
{
  private class Args
  {
    private final List<String> args = new ArrayList<>();

    public void add(String arg)
    {
      args.add(arg);
    }

    public void add(String arg, Object value)
    {
      args.add(arg);
      args.add(value.toString());
    }

    public String[] toArray()
    {
      return args.toArray(new String[args.size()]);
    }

    @Override
    public String toString()
    {
      return args.toString();
    }
  }

  /**
   * Utility method which is called by the testcase sending an ADD request.
   *
   * @param authenticate
   *          The flag to set the authentication on and off.
   * @return The error code of operation performed.
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  private int performAddOperation(boolean authenticate) throws Exception
  {
    String filePath = TestCaseUtils.createTempFile(
        "dn: o=rejectTestCase,o=test", "objectclass: top",
        "objectclass: organization", "o: rejectTestCase",
        "description: Reject Test Case");
    return LDAPModify.mainModify(addArgs(authenticate, filePath), false, null, null);
  }

  private String[] modifyArgs(boolean authenticate, String filePath)
  {
    return args(authenticate, false, filePath);
  }

  private String[] addArgs(boolean authenticate, String filePath)
  {
    return args(authenticate, true, filePath);
  }

  private String[] args(boolean authenticate, boolean add, String filePath)
  {
    Args args = new Args();
    args.add("--noPropertiesFile");
    args.add("-h", "127.0.0.1");
    args.add("-p", TestCaseUtils.getServerLdapPort());
    if (authenticate)
    {
      args.add("-D", "cn=directory manager");
      args.add("-w", "password");
    }
    if (add)
    {
      args.add("-a");
    }
    args.add("-f", filePath);
    return args.toArray();
  }

  /**
   * Utility method which is called by the testcase sending a MODIFY request.
   *
   * @param authenticate
   *          The flag to set the authentication on and off.
   * @return The error code of operation performed.
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  private int performModifyOperation(boolean authenticate) throws Exception
  {
    String path = TestCaseUtils.createTempFile("dn: o=rejectTestCase,o=test",
        "changetype: modify", "replace: description",
        "description: New Description");
    return LDAPModify.mainModify(modifyArgs(authenticate, path), false, null, null);
  }



  /**
   * Utility method which is called by the testcase sending a COMPARE
   * request.
   *
   * @param authentication
   *          The flag to set the authentication on and off.
   * @return The error code of operation performed.
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  private int performCompareOperation(boolean authentication) throws Exception
  {
    return LDAPCompare.mainCompare(compareArgs(authentication), false, null, null);
  }

  private String[] compareArgs(boolean authenticate)
  {
    Args args = new Args();
    args.add("--noPropertiesFile");
    args.add("-h", "127.0.0.1");
    args.add("-p", TestCaseUtils.getServerLdapPort());
    if (authenticate)
    {
      args.add("-D", "cn=Directory Manager");
      args.add("-w", "password");
    }
    args.add("o:test", "o=test");
    return args.toArray();
  }

  /**
   * Utility method which is called by the testcase sending a MODRDN
   * request.
   *
   * @param authentication
   *          The flag to set the authentication on and off.
   * @return The error code of operation performed.
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  private int performModRdnOperation(boolean authentication) throws Exception
  {
    String path = TestCaseUtils
        .createTempFile("dn: o=rejectTestCase,o=Test", "changetype: modrdn",
            "newrdn: o=mod_rejectTestCase", "deleteoldrdn: 0");
    return LDAPModify.mainModify(modRdnArgs(authentication, path), false, null, null);
  }

  private String[] modRdnArgs(boolean authenticate, String path)
  {
    Args args = new Args();
    args.add("--noPropertiesFile");
    args.add("-h", "127.0.0.1");
    args.add("-p", TestCaseUtils.getServerLdapPort());
    if (authenticate)
    {
      args.add("-D", "cn=directory manager");
      args.add("-w", "password");
    }
    args.add("-f", path);
    return args.toArray();
  }

  /**
   * Utility method which is called by the testcase sending a DELETE
   * request.
   *
   * @param authentication
   *          The flag to set the authentication on and off.
   * @return The error code of operation performed.
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  private int performDeleteOperation(boolean authentication) throws Exception
  {
    return LDAPDelete.mainDelete(deleteArgs(authentication), false, null, null);
  }

  private String[] deleteArgs(boolean authenticate)
  {
    Args args = new Args();
    args.add("--noPropertiesFile");
    args.add("-h", "127.0.0.1");
    args.add("-p", TestCaseUtils.getServerLdapPort());
    if (authenticate)
    {
      args.add("-V", "3");
      args.add("-D", "cn=Directory Manager");
      args.add("-w", "password");
    }
    args.add("o=mod_rejectTestCase,o=test");
    return args.toArray();
  }

  /**
   * Ensures that the Directory Server is running before executing the
   * testcases.
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @BeforeClass
  public void startServer() throws Exception
  {
    TestCaseUtils.startServer();
    TestCaseUtils.initializeTestBackend(true);
  }

  private enum Auth
  {
    ANONYMOUS, SIMPLE, START_TLS
  }

  private String[] searchArgs(Auth auth)
  {
    Args args = new Args();
    args.add("--noPropertiesFile");
    args.add("-h", "127.0.0.1");
    args.add("-p", TestCaseUtils.getServerLdapPort());
    if (!Auth.ANONYMOUS.equals(auth))
    {
      args.add("-D", "cn=Directory Manager");
      args.add("-w", "password");
    }
    if (Auth.START_TLS.equals(auth))
    {
      args.add("-q");
      args.add("-X");
    }
    args.add("-b", "");
    args.add("-s", "base");
    args.add("(objectClass=*)");
    return args.toArray();
  }

  /**
   * Tests whether an authenticated SEARCH request will be allowed with the default configuration
   * settings for "ds-cfg-reject-unauthenticated-requests".
   */
  @Test
  public void testAuthSearchDefCfg()
  {
    DirectoryServer.setRejectUnauthenticatedRequests(false);
    assertEquals(LDAPSearch.mainSearch(searchArgs(Auth.SIMPLE), false, null, System.err), 0);
  }

  /**
   * Tests whether an unauthenticated SEARCH request will be allowed with the default configuration
   * settings for "ds-cfg-reject-unauthenticated-requests".
   */
  @Test
  public void testUnauthSearchDefCfg()
  {
    DirectoryServer.setRejectUnauthenticatedRequests(false);
    assertEquals(LDAPSearch.mainSearch(searchArgs(Auth.ANONYMOUS), false, null, System.err), 0);
  }

  /**
   * Tests whether an authenticated BIND request will be allowed with
   * the default configuration settings for
   * "ds-cfg-reject-unauthenticated-requests" .
   */
  @Test
  public void testAuthBindDefCfg()
  {
    DirectoryServer.setRejectUnauthenticatedRequests(false);

    InternalClientConnection conn = new InternalClientConnection(new AuthenticationInfo());
    ByteString user = ByteString.valueOfUtf8("cn=Directory Manager");
    ByteString password = ByteString.valueOfUtf8("password");
    BindOperation bindOperation = conn.processSimpleBind(user, password);
    assertEquals(bindOperation.getResultCode(), ResultCode.SUCCESS);
  }



  /**
   * Tests whether an Unauthenticated BIND request will be allowed
   * with the default configuration settings for
   * "ds-cfg-reject-unauthenticated-requests".
   */
  @Test
  public void testUnauthBindDefCfg()
  {
    DirectoryServer.setRejectUnauthenticatedRequests(false);

    InternalClientConnection conn = new InternalClientConnection(new AuthenticationInfo());
    BindOperation bindOperation = conn.processSimpleBind(DN.rootDN(), null);
    assertEquals(bindOperation.getResultCode(), ResultCode.SUCCESS);
  }



  /**
   * Tests whether the Who Am I? extended operation with an internal
   * authenticated connection succeeds with default setting of
   * "ds-cfg-reject-unauthenticated-requests".
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @Test
  public void testAuthWAIDefCfg() throws Exception
  {
    DirectoryServer.setRejectUnauthenticatedRequests(false);

    try (RemoteConnection conn = new RemoteConnection("localhost", TestCaseUtils.getServerLdapPort()))
    {
      LDAPAuthenticationHandler authHandler = conn.newLDAPAuthenticationHandler();
      authHandler.doSimpleBind(3, ByteString.valueOfUtf8("cn=Directory Manager"),
          ByteString.valueOfUtf8("password"), new ArrayList<Control>(),
          new ArrayList<Control>());
      assertNotNull(authHandler.requestAuthorizationIdentity());

      conn.unbind();
    }
  }



  /**
   * Tests whether the who am I? extended operation with an internal
   * unauthenticated connection succeeds with default setting of
   * "ds-cfg-reject-unauthenticated-requests".
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @Test
  public void testUnauthWAIDefCfg() throws Exception
  {
    DirectoryServer.setRejectUnauthenticatedRequests(false);

    try (RemoteConnection conn = new RemoteConnection("localhost", TestCaseUtils.getServerLdapPort()))
    {
      LDAPAuthenticationHandler authHandler = conn.newLDAPAuthenticationHandler();
      assertNull(authHandler.requestAuthorizationIdentity());
      conn.unbind();
    }
  }



  /**
   * Tests the use of the StartTLS extended operation to communicate
   * with the server in conjunction with no authentication and using
   * blind trust with the default configuration settings.
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @Test
  public void testStartTLSUnauthDefCfg() throws Exception
  {
    DirectoryServer.setRejectUnauthenticatedRequests(false);
    assertEquals(LDAPSearch.mainSearch(searchArgs(Auth.START_TLS), false, null, System.err), 0);
  }

  /**
   * Tests the whether the authenticated ADD,MODIFY,COMPARE,MODRDN and
   * DELETE requests succeed with the default configuration settings.
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @Test
  public void testOtherOpsAuthDefCfg() throws Exception
  {
    DirectoryServer.setRejectUnauthenticatedRequests(false);

    assertEquals(performAddOperation(true), 0);
    assertEquals(performModifyOperation(true), 0);
    assertEquals(performCompareOperation(true), 0);
    assertEquals(performModRdnOperation(true), 0);
    assertEquals(performDeleteOperation(true), 0);
  }



  /**
   * Tests the whether the unauthenticated ADD,MODIFY,COMPARE,MODRDN
   * and DELETE requests succeed with the default configuration
   * settings. FIXME: This test is disabled because it is unreasonable
   * to expect unauthenticated writes to succeed when access control
   * is enabled.
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @Test(enabled = false)
  public void testOtherOpsUnauthDefCfg() throws Exception
  {
    assertEquals(performAddOperation(false), 0);
    assertEquals(performModifyOperation(false), 0);
    assertEquals(performCompareOperation(false), 0);
    assertEquals(performModRdnOperation(false), 0);
    assertEquals(performDeleteOperation(false), 0);
  }



  /**
   * Tests whether both authenticated and unauthenticated SEARCH
   * requests will be allowed with the new configuration settings for
   * "ds-cfg-reject-unauthenticated-requests" .
   */
  @Test
  public void testSearchNewCfg()
  {
    try
    {
      DirectoryServer.setRejectUnauthenticatedRequests(true);

      assertFalse(LDAPSearch.mainSearch(searchArgs(Auth.ANONYMOUS), false, null, null) == 0);
      assertEquals(LDAPSearch.mainSearch(searchArgs(Auth.START_TLS), false, null, System.err), 0);
    }
    finally
    {
      DirectoryServer.setRejectUnauthenticatedRequests(false);
    }
  }

  /**
   * Tests whether authenticated and unauthenticated BIND requests
   * will be allowed with the new configuration settings for
   * "ds-cfg-reject-unauthenticated-requests" .
   */
  @Test
  public void testBindNewCfg()
  {
    try
    {
      DirectoryServer.setRejectUnauthenticatedRequests(true);

      InternalClientConnection conn = new InternalClientConnection(new AuthenticationInfo());
      ByteString user = ByteString.valueOfUtf8("cn=Directory Manager");
      ByteString password = ByteString.valueOfUtf8("password");
      // Unauthenticated BIND request.
      BindOperation bindOperation = conn.processSimpleBind(DN.rootDN(), null);
      assertEquals(bindOperation.getResultCode(), ResultCode.SUCCESS);
      // Authenticated BIND request.
      bindOperation = conn.processSimpleBind(user, password);
      assertEquals(bindOperation.getResultCode(), ResultCode.SUCCESS);
    }
    finally
    {
      DirectoryServer.setRejectUnauthenticatedRequests(false);
    }
  }



  /**
   * Tests the use of the StartTLS extended operation to communicate
   * with the server in conjunction with no authentication and using
   * blind trust.
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @Test
  public void testStartTLSNoAuthTrustAll() throws Exception
  {
    try
    {
      DirectoryServer.setRejectUnauthenticatedRequests(true);

      assertEquals(LDAPSearch.mainSearch(searchArgs(Auth.START_TLS), false, null, System.err), 0);
    }
    finally
    {
      DirectoryServer.setRejectUnauthenticatedRequests(false);
    }
  }



  /**
   * Tests whether the Who Am I? extended operation with an internal
   * authenticated connection succeeds with new setting of
   * "ds-cfg-reject-unauthenticated-requests".
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @Test
  public void testAuthWAINewCfg() throws Exception
  {
    try
    {
      DirectoryServer.setRejectUnauthenticatedRequests(true);

      ExtendedOperation extOp = getRootConnection().processExtendedOperation(OID_WHO_AM_I_REQUEST, null);
      assertEquals(extOp.getResultCode(), ResultCode.SUCCESS);
      assertNotNull(extOp.getResponseValue());
    }
    finally
    {
      DirectoryServer.setRejectUnauthenticatedRequests(false);
    }
  }



  /**
   * Tests whether the who am I? extended operation with an
   * unauthenticated connection fails with new setting of
   * "ds-cfg-reject-unauthenticated-requests".
   */
  @Test
  public void testUnauthWAINewCfg() throws Exception
  {
    try (RemoteConnection conn = new RemoteConnection("localhost", TestCaseUtils.getServerLdapPort()))
    {
      DirectoryServer.setRejectUnauthenticatedRequests(true);

      LDAPAuthenticationHandler authHandler = conn.newLDAPAuthenticationHandler();
      try
      {
        authHandler.requestAuthorizationIdentity();
        fail();
      }
      catch (LDAPException expected)
      {
      }
      finally
      {
        conn.unbind();
      }
    }
    finally
    {
      DirectoryServer.setRejectUnauthenticatedRequests(false);
    }
  }



  /**
   * Tests the whether the authenticated ADD,MODIFY,COMPARE,MODRDN and
   * DELETE requests succeed with the new configuration settings.
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @Test
  public void testOtherOpsAuthNewCfg() throws Exception
  {
    try
    {
      DirectoryServer.setRejectUnauthenticatedRequests(true);

      assertEquals(performAddOperation(true), 0);
      assertEquals(performModifyOperation(true), 0);
      assertEquals(performCompareOperation(true), 0);
      assertEquals(performModRdnOperation(true), 0);
      assertEquals(performDeleteOperation(true), 0);
    }
    finally
    {
      DirectoryServer.setRejectUnauthenticatedRequests(false);
    }
  }



  /**
   * Tests the whether the unauthenticated ADD,MODIFY,COMPARE,MODRDN
   * and DELETE requests fail with the new configuration settings.
   *
   * @throws Exception
   *           If an unexpected problem occurs.
   */
  @Test
  public void testOtherOpsUnauthNewCfg() throws Exception
  {
    try
    {
      DirectoryServer.setRejectUnauthenticatedRequests(true);

      assertNotEquals(performAddOperation(false), 0);
      assertNotEquals(performModifyOperation(false), 0);
      assertNotEquals(performCompareOperation(false), 0);
      assertNotEquals(performModRdnOperation(false), 0);
      assertNotEquals(performDeleteOperation(false), 0);
    }
    finally
    {
      DirectoryServer.setRejectUnauthenticatedRequests(false);
    }
  }
}
