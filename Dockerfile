FROM nginx:latest

COPY finally.html /usr/share/nginx/html/
COPY yes.html /usr/share/nginx/html/
COPY nextpage.html /usr/share/nginx/html/
COPY style.css /usr/share/nginx/html/

EXPOSE 80

RUN chmod 644 /usr/share/nginx/html/finally.html && \
    chmod 644 /usr/share/nginx/html/yes.html && \
    chmod 644 /usr/share/nginx/html/nextpage.html && \
    chmod 644 /usr/share/nginx/html/style.css

CMD ["nginx", "-g", "daemon off;"]
