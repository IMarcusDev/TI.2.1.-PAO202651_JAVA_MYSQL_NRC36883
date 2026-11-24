<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%-- Raíz de la aplicación: va al menú (AuthFilter lleva al inicio de sesión si hace falta). --%>
<% response.sendRedirect(request.getContextPath() + "/menu"); %>
