package com.auth.service.resources;

import com.auth.service.dao.UserDAO;
import com.auth.service.entities.User;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/user")
@Api("/v1/user")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UserResource {

    private final UserDAO userDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    public UserResource(UserDAO userDao) {
        this.userDao = userDao;
    }

    @POST
    @Path("/save")
    @ApiOperation("Save user")
    @UnitOfWork
    @Timed
    public Response find(@Valid User user) {     
        user.setRegisteredDate(new Date(System.currentTimeMillis()));
        userDao.save(user);
        //userDao.doAction();
        return Response.ok().entity(user).type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
    }

    @GET
    @Path("/all")
    @ApiOperation("Find all")
    @UnitOfWork
    @Timed
    public Response findAll() {
        List<User> allUsers = userDao.findAll();
        return Response.ok().entity(allUsers).type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
    }

}
