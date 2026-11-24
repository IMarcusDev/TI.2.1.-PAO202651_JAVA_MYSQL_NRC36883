<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
  Gestión de países (ECUD4): búsqueda por nombre, tabla con Editar y Eliminar por fila,
  formulario (nuevo, edición o devuelto con error) y confirmación de eliminación.
--%>
<c:set var="titulo" value="Países"/>
<c:set var="pantalla" value="paises"/>
<%@ include file="partes/inicio.jspf" %>

<h1 class="titulo">Países</h1>
<p class="descripcion">Países almacenados en la base de datos. Busque por nombre o mantenga el listado.</p>
<%@ include file="partes/avisos.jspf" %>

<c:if test="${not empty eliminar}">
    <div class="tarjeta confirmacion confirmacion--peligro" role="alertdialog" aria-labelledby="pregunta-eliminar">
        <p id="pregunta-eliminar"><strong>¿Eliminar el país <c:out value="${eliminar.nombre}"/>?</strong></p>
        <form method="post" action="${ctx}/paises" class="botones">
            <%@ include file="partes/csrf.jspf" %>
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" name="id" value="${eliminar.id}">
            <input type="hidden" name="confirmado" value="si">
            <button class="btn btn--peligro" type="submit">Eliminar</button>
            <a class="btn btn--secundario" href="${ctx}/paises">Cancelar</a>
        </form>
    </div>
</c:if>

<c:if test="${not empty formulario}">
    <c:set var="nuevo" value="${formulario.id == 0}"/>
    <section class="tarjeta" aria-labelledby="titulo-formulario">
        <h2 class="tarjeta__titulo" id="titulo-formulario">${nuevo ? 'Nuevo país' : 'Datos del país'}</h2>
        <form method="post" action="${ctx}/paises" novalidate>
            <%@ include file="partes/csrf.jspf" %>
            <input type="hidden" name="accion" value="${nuevo ? 'registrar' : 'actualizar'}">
            <input type="hidden" name="id" value="${formulario.id}">
            <div class="formulario">
                <div class="campo">
                    <label for="f-nombre">Nombre *</label>
                    <input id="f-nombre" name="nombre" value="<c:out value='${formulario.nombre}'/>" autofocus>
                </div>
                <div class="campo">
                    <label for="f-capital">Capital</label>
                    <input id="f-capital" name="capital" value="<c:out value='${formulario.capital}'/>">
                </div>
                <div class="campo">
                    <label for="f-region">Región *</label>
                    <select id="f-region" name="region">
                        <option value="">Seleccione...</option>
                        <c:forEach var="r" items="${regiones}">
                            <option value="${r}" ${r == formulario.region ? 'selected' : ''}>${r}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="campo">
                    <label for="f-subregion">Subregión</label>
                    <input id="f-subregion" name="subregion" value="<c:out value='${formulario.subregion}'/>">
                </div>
                <div class="campo">
                    <label for="f-poblacion">Población *</label>
                    <%-- Un valor inválido escrito antes (-1) vuelve vacío para corregirlo. --%>
                    <input id="f-poblacion" name="poblacion" inputmode="numeric"
                           value="${formulario.poblacion >= 0 ? formulario.poblacion : ''}">
                </div>
                <div class="campo">
                    <label for="f-area">Área (km²)</label>
                    <input id="f-area" name="area" inputmode="decimal"
                           value="<c:if test='${formulario.area >= 0}'><fmt:formatNumber value='${formulario.area}' maxFractionDigits='2' groupingUsed='false'/></c:if>">
                </div>
                <div class="campo">
                    <label for="f-moneda">Moneda</label>
                    <input id="f-moneda" name="moneda" value="<c:out value='${formulario.moneda}'/>">
                </div>
            </div>
            <div class="botones">
                <button class="btn" type="submit">Guardar</button>
                <a class="btn btn--secundario" href="${ctx}/paises">Cancelar</a>
            </div>
        </form>
    </section>
</c:if>

<div class="fila-formulario" style="margin-top:16px">
    <form method="get" action="${ctx}/paises" class="fila-formulario" style="flex:1 1 auto">
        <div class="campo">
            <label for="q">Buscar</label>
            <input id="q" name="q" value="<c:out value='${param.q}'/>" placeholder="Nombre o parte del nombre">
        </div>
        <button class="btn" type="submit">Buscar</button>
    </form>
    <a class="btn" href="${ctx}/paises?nuevo">+ Nuevo</a>
</div>

<c:if test="${paises != null}">
    <p class="total">${paises.size()} ${paises.size() == 1 ? 'país' : 'países'}</p>
</c:if>
<c:if test="${not empty paises}">
    <div class="tabla-contenedor">
        <table class="tabla">
            <thead>
            <tr>
                <th scope="col">Nombre</th>
                <th scope="col">Capital</th>
                <th scope="col">Región</th>
                <th scope="col">Subregión</th>
                <th scope="col" class="numero">Población</th>
                <th scope="col" class="numero">Área (km²)</th>
                <th scope="col">Moneda</th>
                <th scope="col"><span class="solo-lectores">Acciones</span></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="p" items="${paises}">
                <tr class="${p.id == formulario.id || p.id == eliminar.id ? 'elegida' : ''}">
                    <td><c:out value="${p.nombre}"/></td>
                    <td><c:out value="${p.capital}"/></td>
                    <td><c:out value="${p.region}"/></td>
                    <td><c:out value="${p.subregion}"/></td>
                    <td class="numero"><fmt:formatNumber value="${p.poblacion}" maxFractionDigits="0"/></td>
                    <td class="numero"><fmt:formatNumber value="${p.area}" maxFractionDigits="2"/></td>
                    <td><c:out value="${p.moneda}"/></td>
                    <td class="acciones">
                        <a class="btn btn--secundario btn--pequeno" href="${ctx}/paises?editar=${p.id}"
                           aria-label="Editar <c:out value='${p.nombre}'/>">Editar</a>
                        <a class="btn btn--peligro btn--pequeno" href="${ctx}/paises?eliminar=${p.id}"
                           aria-label="Eliminar <c:out value='${p.nombre}'/>">Eliminar</a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</c:if>

<%@ include file="partes/fin.jspf" %>
